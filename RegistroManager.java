import java.io.IOException;
import java.io.RandomAccessFile;


public class RegistroManager {
    
    private final String filename;

    public RegistroManager(String filename) {
        this.filename = filename;
    }

    public int getLastId() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            if (file.length() == 0) return 0;
            return file.readInt();
        }
    }

    private void setLastId(int id) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.writeInt(id);
        }
    }

    public void create(Registro registro) throws IOException {
        byte[] data = RegistroUtils.serialize(registro);
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            int lastId = getLastId();
            registro.setId(lastId + 1);
            setLastId(registro.getId());

            file.seek(file.length()); // move to the end of the file
            file.writeByte(1);  // lápide, registro válido
            file.writeInt(data.length); // indicador de tamanho
            file.write(data); // vetor de bytes
        }
    }

    public Registro read(int id) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.seek(4); // skip last id in header
            while (file.getFilePointer() < file.length()) {
                byte lapide = file.readByte();
                int length = file.readInt();
                byte[] data = new byte[length];
                file.read(data);
                Registro registro = RegistroUtils.deserialize(data);
                if (registro.getId() == id && lapide == 1) {
                    return registro;
                }
            }
            return null; // not found
        }
    }

    public void update(Registro updatedRegistro) throws IOException {
        byte[] newData = RegistroUtils.serialize(updatedRegistro);
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.seek(4); // skip last id in header
            while (file.getFilePointer() < file.length()) {
                long pointer = file.getFilePointer();
                byte lapide = file.readByte();
                int length = file.readInt();
                byte[] data = new byte[length];
                file.read(data);
                Registro registro = RegistroUtils.deserialize(data);
                if (registro.getId() == updatedRegistro.getId() && lapide == 1) {
                    file.seek(pointer);
                    file.writeByte(1);  // lápide, registro válido
                    file.writeInt(newData.length); // indicador de tamanho
                    file.write(newData); // vetor de bytes
                    return;
                }
            }
        }
    }

    public void delete(int id) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.seek(4); // skip last id in header
            while (file.getFilePointer() < file.length()) {
                long pointer = file.getFilePointer();
                byte lapide = file.readByte();
                int length = file.readInt();
                byte[] data = new byte[length];
                file.read(data);
                Registro registro = RegistroUtils.deserialize(data);
                if (registro.getId() == id && lapide == 1) {
                    file.seek(pointer);
                    file.writeByte(0);  // lápide, registro inválido
                    return;
                }
            }
        }
    }
}
