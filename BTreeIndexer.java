import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BTreeIndexer {
    private static final String CSV_HEADER = "ID,Address\n";


    public void createIndexFile(String binaryFilePath, String csvFilePath) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r");
             FileWriter csvWriter = new FileWriter(csvFilePath, false)) {  // Append mode for debugging
            csvWriter.append(CSV_HEADER);

            byte[] intBytes = new byte[4];
            raf.read(intBytes);
            ByteBuffer buffer = ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN);

            while (raf.getFilePointer() < raf.length()) {
                long address = raf.getFilePointer();
                //System.out.println("Reading from address: " + address);  // Debugging line

                byte lapide = raf.readByte();
                //System.out.println("Lapide: " + lapide);  // Debugging line

                raf.read(intBytes);
                buffer.rewind();
                int tamanhoRegistro = buffer.getInt();

                //System.out.println("Tamanho do Registro: " + tamanhoRegistro);  // Debugging line

                if (lapide == 1) {
                    byte[] recordData = new byte[tamanhoRegistro];
                    raf.readFully(recordData);
                    
                    Registro record = Registro.fromBytes(recordData);
                    int id = record.getId();  // Assuming Registro has getId()

                    //System.out.println("ID: " + id);  // Debugging line

                    // Write to CSV
                    csvWriter.append(id + "," + address + "\n");
                    csvWriter.flush();  // Debugging line: Ensure immediate write to file
                } else {
                    raf.skipBytes(tamanhoRegistro);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void updateIndexFile(int id, long byteOffset, String csvFilePath) throws IOException {
    try (FileWriter csvWriter = new FileWriter(csvFilePath, true)) {  // Append mode
        csvWriter.append(id + "," + byteOffset + "\n");
    }
}

}


