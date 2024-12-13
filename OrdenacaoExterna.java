
import java.io.*;
import java.util.*;

public class OrdenacaoExterna {

    // Implementing the balanced interleaved method
    public static void balancedInterleavedMethod(String filePath, int m, int n) {
        try {
            PriorityQueue<Registro> minHeap = new PriorityQueue<>(Comparator.comparing(Registro::getNome));
            List<File> tempFiles = new ArrayList<>();
            RegistroFileHandler fileHandler = new RegistroFileHandler();
            List<Registro> sortedRecords = new ArrayList<>();
            
            // Initialize n temporary files and corresponding ObjectOutputStream instances
            ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
            }
            
            int index = 0;
            int id = 1;  // Starting ID for fetching Registro objects
            
            while (true) {
                try {
                    // Take m records from the binary file using getRegistroById
                    for (int i = 0; i < m; i++) {
                        Registro record = fileHandler.getRegistroById(filePath, id++);
                        if (record == null) {
                            throw new EOFException();
                        }
                        minHeap.add(record);
                    }
                } catch (EOFException e) {
                    // End of file reached
                    break;
                }
                
                // Write the sorted m records to the temporary files using single ObjectOutputStream instances
                while (!minHeap.isEmpty()) {
                    Registro minRecord = minHeap.poll();
                    outputStreams[index % n].writeObject(minRecord);
                    index++;
                }
            }
            
            // Close all ObjectOutputStream instances
            for (int i = 0; i < n; i++) {
                outputStreams[i].close();
            }
            
            // Merge the sorted records from the temporary files into an ArrayList
            for (File tempFile : tempFiles) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                    Registro record;
                    while ((record = (Registro) in.readObject()) != null) {
                        sortedRecords.add(record);
                    }
                } catch (EOFException e) {
                    // Do nothing, continue to the next file
                }
            }
            
            // Call saveRegistros method to save the sorted records in database.bin
            fileHandler.saveRegistros(sortedRecords,"database.bin");
            
            // Delete temporary files
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void balancedInterleavedMethod2(String filePath, int m, int n) {
        try {
            List<Registro> records = new ArrayList<>();
            List<File> tempFiles = new ArrayList<>();
            RegistroFileHandler fileHandler = new RegistroFileHandler();
            List<Registro> sortedRecords = new ArrayList<>();
            
            // Initialize n temporary files and corresponding ObjectOutputStream instances
            ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
            for (int i = 0; i < n; i++) {
                File tempFile = File.createTempFile("tempFile" + i, ".tmp");
                tempFiles.add(tempFile);
                outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
            }
            
            int index = 0;
            int id = 1;  // Starting ID for fetching Registro objects
            
            while (true) {
                try {
                    // Take m records from the binary file using getRegistroById
                    for (int i = 0; i < m; i++) {
                        Registro record = fileHandler.getRegistroById(filePath, id++);
                        if (record == null) {
                            throw new EOFException();
                        }
                        records.add(record);
                    }
                    
                    // Sort the records based on getNome
                    records.sort(Comparator.comparing(Registro::getNome));
                    
                    // Write the sorted m records to the temporary files using single ObjectOutputStream instances
                    for (Registro sortedRecord : records) {
                        outputStreams[index % n].writeObject(sortedRecord);
                        index++;
                    }
                    
                    records.clear();  // Clear the list for the next batch of records
                    
                } catch (EOFException e) {
                    // End of file reached
                    break;
                }
            }
            
            // Close all ObjectOutputStream instances
            for (int i = 0; i < n; i++) {
                outputStreams[i].close();
            }
            
            // Merge the sorted records from the temporary files into an ArrayList
            for (File tempFile : tempFiles) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                    Registro record;
                    while ((record = (Registro) in.readObject()) != null) {
                        sortedRecords.add(record);
                    }
                } catch (EOFException e) {
                    // Do nothing, continue to the next file
                }
            }
            
            // Call saveRegistros method to save the sorted records in database.bin
            fileHandler.saveRegistros( sortedRecords,"database.bin");
            
            // Delete temporary files
            for (File tempFile : tempFiles) {
                tempFile.delete();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void balancedInterleavedMethod3(String filePath, int m, int n) {
    try {
        List<Registro> records = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>();
        RegistroFileHandler fileHandler = new RegistroFileHandler();
        List<Registro> sortedRecords = new ArrayList<>();
        
        // Initialize n temporary files and corresponding ObjectOutputStream instances
        ObjectOutputStream[] outputStreams = new ObjectOutputStream[n];
        Registro[] lastRecords = new Registro[n];  // Keep track of the last record in each temp file
        
        for (int i = 0; i < n; i++) {
            File tempFile = File.createTempFile("tempFile" + i, ".tmp");
            tempFiles.add(tempFile);
            outputStreams[i] = new ObjectOutputStream(new FileOutputStream(tempFile, true));
        }
        
        int id = 1;  // Starting ID for fetching Registro objects
        
        while (true) {
            try {
                // Take m records from the binary file using getRegistroById
                for (int i = 0; i < m; i++) {
                    Registro record = fileHandler.getRegistroById(filePath, id++);
                    if (record == null) {
                        throw new EOFException();
                    }
                    records.add(record);
                }
                
                // Sort the records based on getNome
                records.sort(Comparator.comparing(Registro::getNome));
                
                // Determine which temporary file to write to
                int targetIndex = (lastRecords[0] == null) ? 0 : -1;
                
                for (int i = 0; i < n; i++) {
                    if (lastRecords[i] == null || 
                        lastRecords[i].getNome().compareTo(records.get(0).getNome()) <= 0) {
                        targetIndex = i;
                        break;
                    }
                }
                
                // If no suitable temp file is found, use the next one in sequence
                if (targetIndex == -1) {
                    targetIndex = 0;
                }
                
                // Write the sorted m records to the selected temporary file
                for (Registro sortedRecord : records) {
                    outputStreams[targetIndex].writeObject(sortedRecord);
                }
                
                // Update the last record written to this temporary file
                lastRecords[targetIndex] = records.get(records.size() - 1);
                
                records.clear();  // Clear the list for the next batch of records
                
            } catch (EOFException e) {
                // End of file reached
                break;
            }
        }
        
        // Close all ObjectOutputStream instances
        for (int i = 0; i < n; i++) {
            outputStreams[i].close();
        }
        
        // Merge the sorted records from the temporary files into an ArrayList
        for (File tempFile : tempFiles) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile))) {
                Registro record;
                while ((record = (Registro) in.readObject()) != null) {
                    sortedRecords.add(record);
                }
            } catch (EOFException e) {
                // Do nothing, continue to the next file
            }
        }
        
        // Call saveRegistros method to save the sorted records in database.bin
        fileHandler.saveRegistros(sortedRecords, "database.bin");
        
        // Delete temporary files
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
    }
}

}
