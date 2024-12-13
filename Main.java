import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Main {
    private static final RegistroManager manager = new RegistroManager("database.bin");


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RegistroFileHandler registroFileHandler = new RegistroFileHandler();
        OrdenacaoExterna externalSorter=new OrdenacaoExterna();
        List<Registro> registroList = new ArrayList<>();

        while (true) {
            System.out.println("MENU:");
            System.out.println("1. Carregar base de dados");
            System.out.println("2. Ler um registro (ID)");
            System.out.println("3. Atualizar um registro");
            System.out.println("4. Deletar um registro (ID)");
            System.out.println("5. Sair");
            System.out.println("7. Ordenação externa comum");
            System.out.println("8. btree");
            System.out.println("9. hash");
            System.out.println("10. btree delete: ");
            System.out.println("11. btree create: ");
            System.out.println("12. btree update: ");
            System.out.println("13. hash delete: ");
            System.out.println("14. create hash: ");
            System.out.println("15. update hash: ");
            System.out.println("16. ordenacao externa segmento variavel: ");

            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();  // Consumir quebra de linha remanescente

            try {
                switch (opcao) {
                    case 1:
                        System.out.print("Digite o nome do arquivo CSV (inclusive com a extensão .csv): ");
                        String nomeCSV = scanner.next();

                        CSVReader csvReader = new CSVReader();

                        // Aqui é chamada a função que importa os dados do arquivo .csv escolhido
                        registroList = csvReader.importCSV(nomeCSV);
                        System.out.println("Importação concluída com sucesso! " + registroList.size() + " registros importados.");
                        
                        System.out.print("Digite o nome do arquivo onde ficarão salvos os registros: ");
                        String nomeArq = scanner.next();

                        // Para Salvar os registros no arquivo, chamamos a função saveRegistros da classe registroFileHandler
                        registroFileHandler.saveRegistros(registroList, nomeArq);
                        System.out.println("Registros salvos com sucesso!");
                        break;

                    case 2:
                        System.out.print("Digite o nome do arquivo onde foram salvos os registros: ");
                        String nomeArqLer = scanner.next();

                        System.out.print("Digite o ID do registro para busca: ");
                        int id = scanner.nextInt();

                        Registro regEncontrado = null;
                        try {
                            regEncontrado = registroFileHandler.getRegistroById(nomeArqLer, id);
                        } catch (IOException e) {
                            System.out.println("Erro ao acessar o banco de dados: " + e.getMessage());
                            break;
                        }

                        if (regEncontrado != null) {
                            System.out.println(regEncontrado);
                        } else {
                            System.out.println("Registro não encontrado!");
                        }
                        break;

                    case 3:

                        // Primeiro, são pedidos os dados atualizados do registro que deseja modificar
                        System.out.print("Digite o nome do arquivo onde foram salvos os registros: ");
                        String nomeArqAtualizar = scanner.next();

                        System.out.println("Digite o ID do registro a ser atualizado:");
                        int idAtualizar = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("Digite a nova sigla do time (3 letras):");
                        String novaSigla = scanner.nextLine();

                        System.out.println("Digite o novo nome completo do time:");
                        String novoNome = scanner.nextLine();

                        System.out.println("Digite a nova data de criação do time no formato YYYY-MM-DD:");
                        String novaDataCriacao = scanner.nextLine();
                        LocalDate novaData = LocalDate.parse(novaDataCriacao);

                        System.out.println("Qual o número de dados que deseja adicionar ao retrospecto");
                        int numValores = scanner.nextInt();
                        scanner.nextLine();

                        String[] listaRetrospectos = new String[numValores];
                        for (int i = 0; i < numValores; i++) {
                            System.out.println("Digite o valor " + (i + 1) + ":");
                            listaRetrospectos[i] = scanner.nextLine();
                        }

                        System.out.println("Digite o aproveitamento do time:");
                        float novoAproveitamento = scanner.nextFloat();
                        scanner.nextLine();

                        // Aqui é criado um objeto do tipo Registro e posteriormente o dado é atualizado no arquivo salvo
                        Registro registroAtualizado = new Registro(novaSigla, novoNome, novaData, listaRetrospectos, novoAproveitamento, idAtualizar);
                        registroFileHandler.atualizarRegistro(nomeArqAtualizar, registroAtualizado);
                        System.out.println("Registro atualizado com sucesso!");
                        break;

                    case 4:
                        System.out.print("Digite o nome do arquivo onde foram salvos os registros: ");
                        String nomeArqDeletar = scanner.next();
                        System.out.println("Digite o ID do registro a ser deletado:");
                        int idDeletar = scanner.nextInt();
                        registroFileHandler.deletarRegistroPorId(nomeArqDeletar, idDeletar);
                        System.out.println("Registro deletado com sucesso!");
                        break;

                    case 5:
                        System.out.println("Saindo...");
                        scanner.close();
                        System.exit(0);
                        
                    case 6:
                        System.out.print("Digite o caminho do arquivo de entrada: ");
                        String caminhoArquivoEntrada = scanner.next();
                        System.out.print("Digite o valor de m (número de registros por chunk): ");
                        int m = scanner.nextInt();
                        System.out.print("Digite o número de arquivos temporários: ");
                        int n = scanner.nextInt();
                        OrdenacaoExterna.balancedInterleavedMethod(caminhoArquivoEntrada, m, n);
                        System.out.println("Ordenação concluída!");
                        break;

                    case 7:
                        System.out.print("Digite o caminho do arquivo de entrada: ");
                        String caminhoArquivoEntradaOrdenacao = scanner.next();
                        System.out.print("Digite o valor de m (número de registros por chunk): ");
                        int valorM = scanner.nextInt();
                        System.out.print("Digite o número de arquivos temporários: ");
                        int valorN = scanner.nextInt();
                        OrdenacaoExterna.balancedInterleavedMethod2(caminhoArquivoEntradaOrdenacao, valorM, valorN);
                        System.out.println("Ordenação concluída!");
                        break;

                        case 8:
                        BTreeIndexer indexer = new BTreeIndexer();
                        indexer.createIndexFile("arq.bin", "index_file.csv");

                        BTreeSearcher searcher = new BTreeSearcher();
                        //Scanner scanner = new Scanner(System.in);
        
                        System.out.print("Enter the ID you want to search: ");
                        int targetId = scanner.nextInt();

                        searcher.searchAndPrintRecord3("arq.bin", "index_file.csv", targetId);
                        break;
                    case 9:
                        ExtendedHashIndexer indexer2 = new ExtendedHashIndexer();
                        indexer2.createIndexFile("arq.bin", "index_file_hash.csv");
                        System.out.print("Enter the ID you want to search: ");
                        int targetId2 = scanner.nextInt();

                        indexer2.searchAndPrintRecord3("arq.bin", "index_file_hash.csv", targetId2);
                        break;
                    case 10: 
                    System.out.println("=== Delete a Record ===");
    System.out.println("Digite o ID que deseja excluir:");
    Scanner scanner5 = new Scanner(System.in);
    int idToDelete2 = scanner5.nextInt();
    
    try {
        long byteOffset = BTreeSearcher.searchRecord("arq.bin", "index_file.csv", idToDelete2); // Update here
        RegistroFileHandler.deleteRegistroAtIndex(byteOffset, "arq.bin");
        RegistroFileHandler.removeFromIndexFile(idToDelete2, "index_file.csv");
        
        System.out.println("Record with ID " + idToDelete2 + " has been successfully deleted.");
    } catch (IOException e) {
        System.out.println("An error occurred while deleting the record: " + e.getMessage());
    }
    break;
    case 11:
    System.out.println("=== Create a New Record ===");

    // Collecting and setting Sigla
    System.out.println("Digite a sigla:");
    String newSigla = scanner.next();

    // Collecting and setting Nome
    System.out.println("Digite o nome:");
    String newNome = scanner.next();

    // Collecting and setting data (Assuming the format is YYYY-MM-DD)
    System.out.println("Digite a data (format: YYYY-MM-DD):");
    String newDateString = scanner.next();
    LocalDate newData = LocalDate.parse(newDateString);

    // Collecting and setting Restrospecto
    System.out.println("Digite a lista de valores separada por vírgulas (1, 2, 3):");
    String[] newRestrospecto = scanner.next().split(",");

    // Collecting and setting numero
    System.out.println("Digite o aproveitamento:");
    float newNumero = scanner.nextFloat();

    // Initialize a new Registro object with the collected details
    Registro newRecord = new Registro(newSigla, newNome, newData, newRestrospecto, newNumero);

    try {
        long byteOffset = RegistroFileHandler.saveRegistroAtIndex(newRecord, "arq.bin");
        BTreeIndexer.updateIndexFile(newRecord.getId(), byteOffset, "index_file.csv");

        System.out.println("Novo Registro com ID " + newRecord.getId() + " has been successfully created.");
    } catch (IOException e) {
        System.out.println("An error occurred while creating the record: " + e.getMessage());
    }
    break;

    case 12:
    System.out.println("=== Update an Existing Record ===");
    System.out.println("Digite o ID que deseja atualizar:");
    int idToUpdate2 = scanner.nextInt();

    System.out.println("Digite a Sigla:");
    String updatedSigla = scanner.next();

    System.out.println("Digite o nome:");
    String updatedNome = scanner.next();

    System.out.println("Digite a nova data (YYYY-MM-DD):");
    String updatedDateString = scanner.next();
    LocalDate updatedData = LocalDate.parse(updatedDateString);

    System.out.println("Digite o tamanho do retrospecto:");
    int updatedSizeOfRestrospecto = scanner.nextInt();
    String[] updatedRestrospecto = new String[updatedSizeOfRestrospecto];

    for (int i = 0; i < updatedSizeOfRestrospecto; i++) {
        System.out.println("Enter value " + (i + 1) + ":");
        updatedRestrospecto[i] = scanner.next();
    }

    System.out.println("Digite o aproveitamento:");
    float updatedNumero = scanner.nextFloat();

    Registro updatedRecord = new Registro(updatedSigla, updatedNome, updatedData, updatedRestrospecto, updatedNumero, idToUpdate2);

    try {
        RegistroFileHandler.atualizarRegistroComIndex("arq.bin", updatedRecord, "index_file.csv");  // Make sure this method is static
        System.out.println("Registro com ID " + updatedRecord.getId() + "atualizado com sucesso.");
    } catch (IOException e) {
        System.out.println("An error occurred while updating the record: " + e.getMessage());
    }
    break;
    case 13:
    System.out.println("=== Delete a Record ===");
    System.out.println("Digite o ID que deseja excluir:");
    int idToDelete3 = scanner.nextInt();

    try {
        RegistroFileHandler.deleteRecordWithHashIndex(idToDelete3, "arq.bin", "index_file_hash.csv");
        ExtendedHashIndexer.removeFromHashIndex(idToDelete3, "index_file_hash.csv");
        System.out.println("Record with ID " + idToDelete3 + " has been successfully deleted.");
    } catch (IOException e) {
        System.out.println("An error occurred while deleting the record: " + e.getMessage());
    }
    break;
    case 14:
    System.out.println("=== Create a New Record for Hash Index ===");
    
    System.out.println("Digte a Sigla:");
    String Sigla3 = scanner.next();

    System.out.println("Digite o nome:");
    String Nome3 = scanner.next();

    System.out.println("Digite a data (YYYY-MM-DD):");
    String dateString = scanner.next();
    LocalDate data3 = LocalDate.parse(dateString);

    System.out.println("Please enter the size of Lista de Valores (Retrospecto):");
    int sizeOfRestrospecto = scanner.nextInt();
    String[] Restrospecto3 = new String[sizeOfRestrospecto];

    for (int i = 0; i < sizeOfRestrospecto; i++) {
        System.out.println("Please enter value " + (i + 1) + ":");
        Restrospecto3[i] = scanner.next();
    }

    System.out.println("Digite o aproveitamento:");
    float numero3 = scanner.nextFloat();

    // Create the new Registro object
    Registro newRecord3 = new Registro(Sigla3, Nome3, data3, Restrospecto3, numero3);

    try {
        long byteOffset = RegistroFileHandler.saveRegistroAtIndex2(newRecord3, "arq.bin");
        ExtendedHashIndexer.addToHashIndex(newRecord3.getId(), byteOffset, "index_file_hash.csv");  // Assuming this method exists
        
        System.out.println("Novo Registro com ID " + newRecord3.getId() + " criado com sucesso.");
    } catch (IOException e) {
        System.out.println("An error occurred while creating the record: " + e.getMessage());
    }
    break;

    case 15:
    System.out.println("=== Update an Existing Record ===");
    System.out.println("Digite o ID do registro que deseja atualizar:");
    int oldId = scanner.nextInt();

    // Fetch old byte offset using hash index
    long oldByteOffset = ExtendedHashIndexer.searchRecordInHashIndex(oldId, "index_file_hash.csv");
    if (oldByteOffset == -1) {
        System.out.println("ID not found.");
        break;
    }

    // Collecting new details for the Registro object
    System.out.println("Enter new details for the record:");

    System.out.println("Enter new Sigla:");
    String newSigla2 = scanner.next();

    System.out.println("Enter new Nome:");
    String newNome2 = scanner.next();

    System.out.println("Enter new Data (YYYY-MM-DD):");
    String newDateString2 = scanner.next();
    LocalDate newData2 = LocalDate.parse(newDateString2);

    System.out.println("Enter the size of new Restrospecto:");
    int newSizeOfRestrospecto = scanner.nextInt();
    String[] newRestrospecto2 = new String[newSizeOfRestrospecto];
    for (int i = 0; i < newSizeOfRestrospecto; i++) {
        System.out.println("Enter new value for Restrospecto:");
        newRestrospecto2[i] = scanner.next();
    }

    System.out.println("Enter new Aproveitamento:");
    float newNumero2 = scanner.nextFloat();

    Registro updatedRecord2 = new Registro(newSigla2, newNome2, newData2, newRestrospecto2, newNumero2);

    try {
        // Save new record in the binary file and get the new byte offset
        //long newByteOffset = RegistroFileHandler.saveRegistroAtIndex(updatedRecord2, "arq.bin");
        RegistroFileHandler.updateRecordInBinaryFile("arq.bin", updatedRecord2, oldByteOffset);
        // Update the hash index file
        ExtendedHashIndexer.updateHashIndex(oldId, updatedRecord2.getId(), oldByteOffset, "index_file_hash.csv");
        
        System.out.println("Record successfully updated.");
    } catch (IOException e) {
        System.out.println("An error occurred while updating the record: " + e.getMessage());
    }
    break;
    case 16:
                        System.out.print("Enter the path of the input file: ");
                        String inputFilenameVariableSegments2 = scanner.next();
                        System.out.print("Enter the value of m (number of records per chunk): ");
                        int k = scanner.nextInt();
                        System.out.print("Enter the number of temporary files: ");
                        int l = scanner.nextInt();  // This will be used when implementing other sorting methods that require n
                        externalSorter.balancedInterleavedMethod3(inputFilenameVariableSegments2,k,l);
                        System.out.println("Sorting completed!");
                        break;







                        
                    default:
                        System.out.println("Opção inválida!");
                }
            } catch (IOException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }
}
