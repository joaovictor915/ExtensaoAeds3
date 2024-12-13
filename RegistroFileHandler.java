import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RegistroFileHandler {

    private static final byte REGISTRO_VALIDO = 1;
    private static final byte REGISTRO_INVALIDO = 0;

    public void saveRegistros(List<Registro> registros, String arquivo) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivo))) {
            int ultimoId = registros.size() > 0 ? registros.get(registros.size() - 1).getId() : 0;

            // Gravando o último ID utilizando big-endian
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(ultimoId);
            out.write(buffer.array());

            for (Registro registro : registros) {
                // Lápide
                out.writeByte(REGISTRO_VALIDO);

                // Calcula o tamanho do Registro com antecedência
                int tamanhoRegistro = 4; // para ID
                tamanhoRegistro += 2 + registro.getSigla().getBytes("UTF-8").length; // para sigla
                tamanhoRegistro += 2 + registro.getNome().getBytes("UTF-8").length; // para nome
                tamanhoRegistro += 8; // para epoch day do LocalDate
                tamanhoRegistro += 4; // para tamanho do retrospecto
                for (String valor : registro.getRetrospecto()) {
                    tamanhoRegistro += 2 + valor.getBytes("UTF-8").length; // para cada valor no retrospecto
                }
                tamanhoRegistro += 4; // para aproveitamento

                // Escreve o tamanho do Registro logo após a Lápide
                out.writeInt(tamanhoRegistro);

                // Agora escreve o conteúdo real utilizando big-endian onde necessário
                buffer.clear();
                buffer.putInt(registro.getId());
                out.write(buffer.array());

                out.writeUTF(registro.getSigla());
                out.writeUTF(registro.getNome());

                // Usa um ByteBuffer separado para o valor long
                ByteBuffer longBuffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
                LocalDate data = (registro.getData() != null) ? registro.getData() : LocalDate.now();
                longBuffer.putLong(data.toEpochDay());
                out.write(longBuffer.array());

                buffer.clear();
                buffer.putInt(registro.getsizeRetrospecto());
                out.write(buffer.array());

                for (String valor : registro.getRetrospecto()) {
                    out.writeUTF(valor);
                }

                buffer.clear();
                buffer.putFloat(registro.getAproveitamento());
                out.write(buffer.array());
            }
        }
    }

    public Registro getRegistroById(String arquivo, int idDesejado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            // Lê o último ID salvo utilizando big-endian
            byte[] intBytes = new byte[4];
            raf.read(intBytes);
            ByteBuffer buffer = ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN);

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                
                // Lê o tamanho do registro utilizando big-endian
                raf.read(intBytes);
                buffer.rewind();

                int tamanhoRegistro = buffer.getInt();

                // Se é um registro válido (lapide == 1)
                if (lapide == REGISTRO_VALIDO) { 
                    byte[] registroBytes = new byte[tamanhoRegistro];
                    raf.readFully(registroBytes);

                    // Converte os bytes em um objeto Registro (supondo que Registro tenha tal método)
                    Registro registro = Registro.fromBytes(registroBytes);
                    
                    if (registro.getId() == idDesejado) {
                        return registro;
                    }
                } else {
                    // Se é um registro inválido, pula para o próximo registro
                    raf.skipBytes(tamanhoRegistro);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Para informações detalhadas da exceção
            throw new RuntimeException("Erro ao acessar o banco de dados: " + e.getMessage());
        }

        return null; // Retorna null se não encontrado
    }

    public void deletarRegistroPorId(String arquivo, int idDesejado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            // Pula o último ID salvo
            raf.skipBytes(4);

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide == REGISTRO_VALIDO) {
                    // Armazena a posição atual
                    long startPos = raf.getFilePointer();

                    // Lê o ID para verificar se corresponde ao ID desejado
                    int currentId = raf.readInt();

                    // Se corresponde, atualiza a lápide e interrompe
                    if (currentId == idDesejado) {
                        raf.seek(startPos - 5);  // -4 para o ID e -1 para mover-se à lápide
                        raf.writeByte(REGISTRO_INVALIDO);
                        break;
                    } else {
                        // Se não corresponde, pula o restante do registro
                        raf.skipBytes(tamanhoRegistro - 4);  // -4 porque já lemos o ID
                    }
                } else {
                    // Se é um registro inválido, pula para o próximo registro
                    raf.skipBytes(tamanhoRegistro);
                }
            }
        }
    }

    public void atualizarRegistro(String arquivo, Registro novoRegistro) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {

            while (raf.getFilePointer() < raf.length()) {
                long startPos = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide == REGISTRO_VALIDO && tamanhoRegistro > 0) {
                    byte[] registroBytes = new byte[tamanhoRegistro];
                    raf.readFully(registroBytes);

                    Registro registroAtual = Registro.fromBytes(registroBytes);

                    if (registroAtual.getId() == novoRegistro.getId()) {
                        byte[] novoRegistroBytes = novoRegistro.toBytes();
                        int newRegistroSize = novoRegistroBytes.length;

                        if (newRegistroSize <= tamanhoRegistro) {
                            // Atualiza no lugar
                            raf.seek(startPos);
                            raf.writeByte(REGISTRO_VALIDO);
                            raf.writeInt(newRegistroSize);
                            raf.write(novoRegistroBytes);
                        } else {
                            // Marca o registro atual como inválido
                            raf.seek(startPos);
                            raf.writeByte(REGISTRO_INVALIDO);

                            // Atualiza o tamanho do registro antigo SEM mover o ponteiro do arquivo de volta
                            raf.writeInt(tamanhoRegistro);

                            // Escreve o novo registro no final do arquivo
                            raf.seek(raf.length());
                            raf.writeByte(REGISTRO_VALIDO);
                            raf.writeInt(newRegistroSize);
                            raf.write(novoRegistroBytes);
                        }

                        return;
                    }
                }
            }
        }
    }

    public void deleteRecord(int id) throws IOException {
        long byteOffset = BTreeSearcher.searchRecord("arq.bin", "index_file.csv", id);
        RegistroFileHandler.deleteRegistroAtIndex(byteOffset, "arq.bin");
        removeFromIndexFile(id, "index_file.csv");
    }

    public static void deleteRegistroAtIndex(long byteOffset, String arquivo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            raf.seek(byteOffset);

            // Change the "lapide" byte to mark the record as invalid
            raf.writeByte(REGISTRO_INVALIDO);
        }
    }

    public static void removeFromIndexFile(int id, String csvFilePath) throws IOException {
        File inputFile = new File(csvFilePath);
        File tempFile = new File("temp.csv");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            reader.readLine();
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                String[] parts = currentLine.split(",");
                if (parts.length > 0 && Integer.parseInt(parts[0]) == id) {
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
        }

        // Delete the original file and rename the temp file
        if (!inputFile.delete()) {
            System.out.println("Could not delete file");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file");
        }
    }

    public void createRecord(Registro newRecord) throws IOException {
        long byteOffset = RegistroFileHandler.saveRegistroAtIndex(newRecord, "arq.bin");
        BTreeIndexer.updateIndexFile(newRecord.getId(), byteOffset, "index_file.csv");
    }

    public static long saveRegistroAtIndex(Registro newRecord, String arquivo) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivo, true))) {
            // Move to the end of the file to get the byte offset
            long byteOffset = out.size();

            // Lápide
            out.writeByte(REGISTRO_VALIDO);

            // Calculate the size of Registro in advance
            int tamanhoRegistro = 4; // for ID
            tamanhoRegistro += 2 + newRecord.getSigla().getBytes("UTF-8").length; // for Sigla
            tamanhoRegistro += 2 + newRecord.getNome().getBytes("UTF-8").length; // for Nome
            tamanhoRegistro += 8; // for epoch day of LocalDate
            tamanhoRegistro += 4; // for size of Restrospecto
            for (String valor : newRecord.getRetrospecto()) {
                tamanhoRegistro += 2 + valor.getBytes("UTF-8").length; // for each valor in Restrospecto
            }
            tamanhoRegistro += 4; // for numero

            // Write the size of Registro right after Lapide
            out.writeInt(tamanhoRegistro);

            // Now write the actual content using big-endian where necessary
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(newRecord.getId());
            out.write(buffer.array());

            out.writeUTF(newRecord.getSigla());
            out.writeUTF(newRecord.getNome());

            // Use a separate ByteBuffer for the long value
            ByteBuffer longBuffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
            LocalDate date = (newRecord.getData() != null) ? newRecord.getData() : LocalDate.now();
            longBuffer.putLong(date.toEpochDay());
            out.write(longBuffer.array());

            buffer.clear();
            buffer.putInt(newRecord.getsizeRetrospecto());
            out.write(buffer.array());

            for (String valor : newRecord.getRetrospecto()) {
                out.writeUTF(valor);
            }

            buffer.clear();
            buffer.putFloat(newRecord.getAproveitamento());
            out.write(buffer.array());

            return byteOffset;
        }
    }

    public static void atualizarRegistroComIndex(String arquivo, Registro novoRegistro, String indexFilePath)
            throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            long byteOffset = BTreeSearcher.searchRecord("arq.bin", indexFilePath, novoRegistro.getId());

            if (byteOffset != -1) { // Record exists
                raf.seek(byteOffset);
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();

                if (lapide == REGISTRO_VALIDO) {
                    byte[] novoRegistroBytes = novoRegistro.toBytes();
                    int newRegistroSize = novoRegistroBytes.length;

                    if (newRegistroSize <= tamanhoRegistro) {
                        // Update in place
                        raf.seek(byteOffset);
                        raf.writeByte(REGISTRO_VALIDO);
                        raf.writeInt(newRegistroSize);
                        raf.write(novoRegistroBytes);
                    } else {
                        // Mark current record as invalid
                        raf.seek(byteOffset);
                        raf.writeByte(REGISTRO_INVALIDO);

                        // Write new record to the end of the file
                        raf.seek(raf.length());
                        long newByteOffset = raf.getFilePointer();
                        raf.writeByte(REGISTRO_VALIDO);
                        raf.writeInt(newRegistroSize);
                        raf.write(novoRegistroBytes);

                        // Update the index file
                        BTreeIndexer.updateIndexFile(novoRegistro.getId(), newByteOffset, indexFilePath); // Assuming
                                                                                                          // this method
                                                                                                          // exists
                    }
                } else {
                    // Handle invalid records if necessary
                }
            } else {
                // Handle case where record does not exist
            }
        }
    }

    public static void deleteRecordWithHashIndex(int id, String binaryFilePath, String hashIndexFilePath)
            throws IOException {
        long byteOffset = ExtendedHashIndexer.searchRecordInHashIndex(id, hashIndexFilePath); // Assuming this method
                                                                                              // exists and returns the
                                                                                              // byte offset
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            raf.seek(byteOffset);
            raf.writeByte(REGISTRO_INVALIDO); // Assuming REGISTRO_INVALIDO is the byte that marks a record as deleted
        }
    }

    public static long saveRegistroAtIndex2(Registro newRecord, String arquivo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "rw")) {
            // Move to the end of the file to get the byte offset
            long byteOffset = raf.length();
            raf.seek(byteOffset);

            // Write the lapide
            raf.writeByte(REGISTRO_VALIDO);

            // Calculate the size of Registro in advance
            int tamanhoRegistro = 4; // for ID
            tamanhoRegistro += 2 + newRecord.getSigla().getBytes("UTF-8").length; // for Sigla
            tamanhoRegistro += 2 + newRecord.getNome().getBytes("UTF-8").length; // for Nome
            tamanhoRegistro += 8; // for epoch day of LocalDate
            tamanhoRegistro += 4; // for size of Restrospecto
            for (String valor : newRecord.getRetrospecto()) {
                tamanhoRegistro += 2 + valor.getBytes("UTF-8").length; // for each valor in Restrospecto
            }
            tamanhoRegistro += 4; // for numero

            // Write the size of Registro right after Lapide
            raf.writeInt(tamanhoRegistro);

            // Now write the actual content using big-endian where necessary
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(newRecord.getId());
            raf.write(buffer.array());

            raf.writeUTF(newRecord.getSigla());
            raf.writeUTF(newRecord.getNome());

            // Use a separate ByteBuffer for the long value
            ByteBuffer longBuffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
            LocalDate date = (newRecord.getData() != null) ? newRecord.getData() : LocalDate.now();
            longBuffer.putLong(date.toEpochDay());
            raf.write(longBuffer.array());

            buffer.clear();
            buffer.putInt(newRecord.getsizeRetrospecto());
            raf.write(buffer.array());

            for (String valor : newRecord.getRetrospecto()) {
                raf.writeUTF(valor);
            }

            buffer.clear();
            buffer.putFloat(newRecord.getAproveitamento());
            raf.write(buffer.array());

            return byteOffset;
        }
    }

    public static void updateRecordInBinaryFile(String binaryFilePath, Registro updatedRecord, long byteOffset)
            throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            byte[] updatedRecordBytes = updatedRecord.toBytes();
            int newSize = updatedRecordBytes.length;

            System.out.println("New record size: " + newSize); // Debugging

            raf.seek(byteOffset);
            byte lapide = raf.readByte();
            int oldSize = raf.readInt();

            System.out.println("Old record size: " + oldSize); // Debugging

            if (newSize <= oldSize) {
                // Update in-place
                raf.seek(byteOffset);
                raf.writeByte(1); // Valid record
                raf.writeInt(newSize);
                raf.write(updatedRecordBytes);
                System.out.println("Record updated in-place."); // Debugging
            } else {
                // Add new record at the end and update index
                long newByteOffset = raf.length();
                raf.seek(newByteOffset);
                raf.writeByte(1); // Valid record
                raf.writeInt(newSize);
                raf.write(updatedRecordBytes);
                System.out.println("New record added at the end. New byte offset: " + newByteOffset); // Debugging

                // Mark the old record as deleted
                raf.seek(byteOffset);
                raf.writeByte(0); // Invalid record
                System.out.println("Old record marked as deleted."); // Debugging
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage()); // Debugging
            throw e;
        }
    }
}
