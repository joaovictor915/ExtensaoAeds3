import java.io.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BTreeSearcher {
    public void searchAndPrintRecord(String binaryFilePath, String csvFilePath, int targetId) {
        try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r");
             BufferedReader csvReader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            long address = -1;

            // Search for the address of the target ID in the CSV file
            while ((line = csvReader.readLine()) != null) {
                String[] values = line.split(",");
                //int id = Integer.parseInt(values[0]);
                int id = (int) Double.parseDouble(values[0]);
                if (id == targetId) {
                    address = Long.parseLong(values[1]);
                    break;
                }
            }

            // If address is found, read and print the record
            if (address != -1) {
                raf.seek(address);

                byte lapide = raf.readByte();
                if (lapide == 0) {
                    System.out.println("Record is deleted or invalid.");
                    return;
                }

                int tamanhoRegistro = raf.readInt();
                byte[] recordData = new byte[tamanhoRegistro];
                raf.readFully(recordData);

                // Your logic for printing the record goes here, e.g.,
                String recordString = new String(recordData);
                System.out.println("Record: " + recordString);
            } else {
                System.out.println("ID not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void searchAndPrintRecord2(String binaryFilePath, String csvFilePath, int targetId) {
    try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r");
         BufferedReader csvReader = new BufferedReader(new FileReader(csvFilePath));
         FileWriter csvWriter = new FileWriter(csvFilePath, true);
         BufferedWriter bufferedWriter = new BufferedWriter(csvWriter)) {

        String line;
        long address = -1;

        // Search for the address of the target ID in the binary file
        byte[] intBytes = new byte[4];
        raf.read(intBytes);
        ByteBuffer buffer = ByteBuffer.wrap(intBytes).order(ByteOrder.BIG_ENDIAN);

        while (raf.getFilePointer() < raf.length()) {
            address = raf.getFilePointer();
            byte lapide = raf.readByte();
            
            raf.read(intBytes);
            buffer.rewind();
            int tamanhoRegistro = buffer.getInt();
            
            if (lapide == 0) {  // Assuming 0 is for valid records
                byte[] recordData = new byte[tamanhoRegistro];
                raf.readFully(recordData);

                // Assuming Registro has a method fromBytes to convert bytes to a Registro object
                Registro registro = Registro.fromBytes(recordData);

                if (registro.getId() == targetId) {
                    // Write address to CSV
                    bufferedWriter.write(targetId + "," + address);
                    bufferedWriter.newLine();

                    // Print Record
                    System.out.println("Record: " + registro.toString());
                    break;
                }
            } else {
                raf.skipBytes(tamanhoRegistro);
            }
        }

        if (address == -1) {
            System.out.println("ID not found.");
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}
public void searchAndPrintRecord3(String binaryFilePath, String csvFilePath, int targetId) {
    try (RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r");
         BufferedReader csvReader = new BufferedReader(new FileReader(csvFilePath))) {
        String line;
        long address = -1;

        // Skip CSV header
        csvReader.readLine();

        // Search for the address in CSV
        while ((line = csvReader.readLine()) != null) {
            String[] values = line.split(",");
            int id = Integer.parseInt(values[0]);
            if (id == targetId) {
                address = Long.parseLong(values[1]);
                break;
            }
        }

        // Read and print the record
        if (address != -1) {
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
        } else {
            System.out.println("ID not found.");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
public static long searchRecord(String binaryFilePath, String csvFilePath, int targetId) {
    long address = -1; // Initialize address as -1 to indicate "not found"

    try (
        RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "r");
        BufferedReader csvReader = new BufferedReader(new FileReader(csvFilePath))
    ) {
        String line;

        // Skip CSV header
        csvReader.readLine();

        // Search for the address in CSV
        while ((line = csvReader.readLine()) != null) {
            String[] values = line.split(",");
            int id = Integer.parseInt(values[0]);
            if (id == targetId) {
                address = Long.parseLong(values[1]);
                break;
            }
        }

        // If address is still -1, the record was not found
        if (address == -1) {
            return -1;
        }

        // Logic to read the record at the found address (if needed)

    } catch (IOException e) {
        e.printStackTrace();
    }

    return address;
}


    
}
