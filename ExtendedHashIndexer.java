import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ExtendedHashIndexer {
    private static  int P = 4;  // Initial hash size exponent
    private static final int TOTAL_RECORDS = 1000;  // Example total records
    private static final int BUCKET_SIZE = (int) Math.ceil(TOTAL_RECORDS * 0.05);  // 5% of total records

    public void createIndexFile(String binaryFilePath, String indexFilePath) {
        HashMap<Integer, List<String>> buckets = new HashMap<>();
        
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
            byte[] intBytes = new byte[4];
            ByteBuffer buffer = ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN);
            
            while (raf.getFilePointer() < raf.length()) {
                long address = raf.getFilePointer();
                
                byte lapide = raf.readByte();
                
                raf.read(intBytes);
                buffer.rewind();
                int tamanhoRegistro = buffer.getInt();

                if (lapide == 1) {
                    byte[] recordData = new byte[tamanhoRegistro];
                    raf.readFully(recordData);
                    
                    Registro record = Registro.fromBytes(recordData);  // Assuming Registro class is defined
                    int id = record.getId();  // Assuming Registro has getId() method
                    
                    //System.out.println("Current ID: " + id);

                    int bucketIndex = id % (int) Math.pow(2, P);
                    //System.out.println("Calculated bucket index: " + bucketIndex);
                    List<String> bucket = buckets.getOrDefault(bucketIndex, new ArrayList<>());
                    
                    if (bucket.size() >= BUCKET_SIZE) {
                        //System.out.println("Bucket is full. Extend hashing mechanism should be implemented.");
                        //return;
                        // Extend Hashing Mechanism
                    P++;  // Double the directory size
                    HashMap<Integer, List<String>> newBuckets = new HashMap<>();

                    for (Map.Entry<Integer, List<String>> entry : buckets.entrySet()) {
                        int newBucketIndex = entry.getKey();
                        List<String> newBucket = new ArrayList<>(entry.getValue());
                        newBuckets.put(newBucketIndex, newBucket);
                        
                        newBucketIndex += Math.pow(2, P-1);  // New bucket index after doubling
                        newBuckets.put(newBucketIndex, new ArrayList<>());  // Empty bucket
                    }

                    buckets = newBuckets;  // Replace old buckets with new extended buckets
                    bucket = buckets.get(bucketIndex);  // Re-fetch the bucket
                    }
                    //System.out.println("Current size of bucket " + bucketIndex + ": " + bucket.size());
        
                    bucket.add(id + " " + address);
                    buckets.put(bucketIndex, bucket);
                    //System.out.println("Updated size of bucket " + bucketIndex + ": " + bucket.size());
                }
            }

            // Prepare FileWriter for CSV output
            try (FileWriter indexWriter = new FileWriter(indexFilePath)) {
                // CSV header
                indexWriter.append("Bucket\n");

                // Iterate through each bucket to write its content to the CSV
                for (List<String> bucket : buckets.values()) {
                    StringBuilder bucketLine = new StringBuilder();
                    for (String id_address_pair : bucket) {
                        bucketLine.append(id_address_pair).append(",");
                    }
                    // Remove trailing comma
                    if (bucketLine.length() > 0) {
                        bucketLine.deleteCharAt(bucketLine.length() - 1);
                    }
                    indexWriter.append(bucketLine.toString()).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void searchAndPrintRecord3(String binaryFilePath, String indexFilePath, int targetId) {
    try (BufferedReader indexReader = new BufferedReader(new FileReader(indexFilePath));
         RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
        long address = -1;
        String line;
        // Skip CSV header
        indexReader.readLine();

        boolean found = false;  // Variável de controle

        while (!found && (line = indexReader.readLine()) != null) {  // Sai do loop quando 'found' for true
            String[] idAddressPairs = line.split(",");  // Divide a linha por vírgula
            
            for (String pair : idAddressPairs) {
                String[] parts = pair.split(" ");  // Divide o id e o endereço
                int id = Integer.parseInt(parts[0]);
                
                if (id == targetId) {
                    address = Long.parseLong(parts[1]);
                    found = true;  // Marca como encontrado
                    break;  // Sai do loop interno
                }
            }
        }
        
        if(address != -1) {
                    // Found the target ID, seek to the address in binary file
                    raf.seek(address);
                    byte lapide = raf.readByte();
                    if (lapide != 1) {
                        System.out.println("Record is deleted or invalid.");
                        return;
                    }
                    int tamanhoRegistro = raf.readInt();
                    byte[] recordData = new byte[tamanhoRegistro];
                    raf.readFully(recordData);

                    Registro record = Registro.fromBytes(recordData);
                    System.out.println("Record: " + record);
                    // Read the record at this address (assuming a method exists to do so)
                    //Registro record = readRecordFromBinaryFile(raf);
                    
                    // Print the record (assuming a toString method exists in Registro class)
                    //System.out.println("Found Record: " + record);
                    
                    return;
        }
        else {
            System.out.println("ID not found.");
        }
            }
        
     catch (IOException e) {
        e.printStackTrace();
    }
    }
    public static long searchRecordInHashIndex(int targetId, String hashIndexFilePath) throws IOException {
    long address = -1;
    try (BufferedReader indexReader = new BufferedReader(new FileReader(hashIndexFilePath))) {
        String line;
        // Skip CSV header
        indexReader.readLine();

        while ((line = indexReader.readLine()) != null) {
            String[] idAddressPairs = line.split(",");  // Split the line by comma
            
            for (String pair : idAddressPairs) {
                String[] parts = pair.split(" ");  // Split the id and address
                int id = Integer.parseInt(parts[0]);
                
                if (id == targetId) {
                    address = Long.parseLong(parts[1]);
                    return address;  // Return the address if found
                }
            }
        }
    }
    return address;  // Will return -1 if not found
}

public static void removeFromHashIndex(int targetId, String hashIndexFilePath) throws IOException {
    File inputFile = new File(hashIndexFilePath);
    File tempFile = new File("temp_hash_index.csv");

    try (
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
    ) {
        // Read and skip the first line (header, if applicable)
        String currentLine = reader.readLine();
        if (currentLine != null) {
            writer.write(currentLine); // Preservar o cabeçalho
            writer.write(System.getProperty("line.separator"));
        }
        
        while ((currentLine = reader.readLine()) != null) {
            StringBuilder updatedLine = new StringBuilder();
            String[] idAddressPairs = currentLine.split(",");  // Split the line by comma
            
            for (String pair : idAddressPairs) {
                String[] parts = pair.split(" ");  // Split the id and address
                int id = Integer.parseInt(parts[0]);
                
                // Skip the entry if it matches the targetId
                if (id != targetId) {
                    updatedLine.append(pair).append(",");
                }
            }
            
            // Remove trailing comma and write the updated line
            if (updatedLine.length() > 0) {
                updatedLine.deleteCharAt(updatedLine.length() - 1);  // Remove trailing comma
                writer.write(updatedLine.toString());
                writer.write(System.getProperty("line.separator"));
            }
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

public static void addToHashIndex(int newId, long newAddress, String indexFilePath) {
    HashMap<Integer, List<String>> buckets = new HashMap<>();
    int P = 4;  // Assuming P is 4, as it wasn't provided
    int BUCKET_SIZE = 5;  // Assuming BUCKET_SIZE is 5, as it wasn't provided

    // Step 1: Load the existing buckets into memory
    try (BufferedReader indexReader = new BufferedReader(new FileReader(indexFilePath))) {
        String line;
        int bucketIndex = 0;

        // Skip header
        indexReader.readLine();

        while ((line = indexReader.readLine()) != null) {
            List<String> bucket = Arrays.asList(line.split(","));
            buckets.put(bucketIndex++, new ArrayList<>(bucket));
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    // Step 2: Calculate the appropriate bucket for the new record
    int targetBucketIndex = newId % (int) Math.pow(2, P);

    // Step 3: Check if the bucket has space and add the new entry
    List<String> targetBucket = buckets.getOrDefault(targetBucketIndex, new ArrayList<>());
    
    if (targetBucket.size() >= BUCKET_SIZE) {
        System.out.println("Bucket is full. Extend hashing mechanism should be implemented.");
        return;
    }

    targetBucket.add(newId + " " + newAddress);
    buckets.put(targetBucketIndex, targetBucket);

    // Step 4: Update the index file with the new bucket information
    try (FileWriter indexWriter = new FileWriter(indexFilePath)) {
        // CSV header
        indexWriter.append("Bucket\n");

        // Iterate through each bucket to write its content to the CSV
        for (List<String> bucket : buckets.values()) {
            StringBuilder bucketLine = new StringBuilder();
            for (String idAddressPair : bucket) {
                bucketLine.append(idAddressPair).append(",");
            }
            // Remove trailing comma
            if (bucketLine.length() > 0) {
                bucketLine.deleteCharAt(bucketLine.length() - 1);
            }
            indexWriter.append(bucketLine.toString()).append("\n");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
public static void updateHashIndex(int oldId, int newId, long newByteOffset, String indexFilePath) throws IOException {
    // Debug: Print oldId, newId, and newByteOffset
    System.out.println("Debug: oldId=" + oldId + ", newId=" + newId + ", newByteOffset=" + newByteOffset);

    List<String> lines = Files.readAllLines(Paths.get(indexFilePath));
    List<String> updatedLines = new ArrayList<>();

    // Preserve the first line (header)
    if (!lines.isEmpty()) {
        updatedLines.add(lines.get(0));
    }

    // Start iterating from the second line
    for (int i = 1; i < lines.size(); i++) {
        String line = lines.get(i);
        String[] pairs = line.split(",");
        StringBuilder updatedLine = new StringBuilder();

        // Debug: Print the line before modification
        System.out.println("Debug: Before Modification: " + line);

        for (String pair : pairs) {
            String[] parts = pair.split(" ");
            int id = Integer.parseInt(parts[0]);

            if (id == oldId) {
                updatedLine.append(newId).append(" ").append(newByteOffset).append(",");
            } else {
                updatedLine.append(pair).append(",");
            }
        }

        if (updatedLine.length() > 0) {
            updatedLine.deleteCharAt(updatedLine.length() - 1);  // Remove trailing comma
        }

        // Debug: Print the line after modification
        System.out.println("Debug: After Modification: " + updatedLine.toString());

        updatedLines.add(updatedLine.toString());
    }

    // Write back to index file
    Files.write(Paths.get(indexFilePath), updatedLines);
}

public void searchAndPrintRecord4(String binaryFilePath, String indexFilePath, int targetId) {
    try (BufferedReader indexReader = new BufferedReader(new FileReader(indexFilePath));
         RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r")) {
        long address = -1;
        String line;
        // Skip CSV header
        indexReader.readLine();

        while ((line = indexReader.readLine()) != null) {
            System.out.println("Debug: Reading line from index file: " + line);  // Debug
            String[] idAddressPairs = line.split(",");  // Split the line by comma
            
            for (String pair : idAddressPairs) {
                if (pair.trim().isEmpty()) {
                    continue;  // Skip empty or whitespace-only strings
                }

                String[] parts = pair.split(" ");  // Split the id and address
                int id = Integer.parseInt(parts[0]);
                
                System.out.println("Debug: Checking ID: " + id);  // Debug

                if (id == targetId) {
                    System.out.println("Debug: ID matched");  // Debug
                    address = Long.parseLong(parts[1]);
                    break;
                }
            }
        }

        if (address != -1) {
            // Found the target ID, seek to the address in the binary file
            // ... (rest of your existing code)
        } else {
            System.out.println("ID not found.");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}





}


