import java.time.LocalDate;
import java.util.StringJoiner;
import java.io.*;

public class Registro implements Serializable{

    private static int nextId = 1;

    private int id;
    private char[] sigla = new char[50];
    private String nome;
    private LocalDate data;
    private int sizeRetrospecto;
    private String[] retrospecto;
    private float aproveitamento;

    // ... other getters and setters ...
    public int getId() {
    return this.id;
}

public void setId(int id) {
    this.id = id;
}

public String getNome() {
    return this.nome;
}

public void setNome(String nome) {
    this.nome = nome;
}

public LocalDate getData() {
    return this.data;
}

public void setData(LocalDate data) {
    this.data = data;
}

public String[] getRetrospecto() {
    return this.retrospecto;
}

public void setRetrospecto(String[] retrospecto) {
    this.retrospecto = retrospecto;
    this.sizeRetrospecto = (retrospecto != null) ? retrospecto.length : 0;
}

public float getAproveitamento() {
    return this.aproveitamento;
}

public void setAproveitamento(float aproveitamento) {
    this.aproveitamento = aproveitamento;
}

public int getsizeRetrospecto() {
    return this.sizeRetrospecto;
}

// No setter for sizeRetrospecto as it is derived from the size of retrospecto array.


    // Construtor de 5 parâmetros
    public Registro(String sigla, String nome, LocalDate data, String[] retrospecto2, float aproveitamento) {
        this.id = nextId++;
        this.setSigla(sigla);
        this.nome = nome;
        this.data = data;
        this.setRetrospecto(retrospecto2);
        this.aproveitamento = aproveitamento;
    }

    // Construtor de 6 parâmetros
    public Registro(String sigla, String nome, LocalDate data, String[] retrospecto, float aproveitamento, int id) {
        this(sigla, nome, data, retrospecto, aproveitamento); // Chama o construtor de 5 parâmetros
        this.id = id;  // Sobrescreve o id
    }

    // Demais métodos, como getters e setters...

    public String getSigla() {
        return new String(this.sigla).trim();
    }

    public void setSigla(String sigla) {
        if (sigla.length() > 50) {
            throw new IllegalArgumentException("String too long!");
        }
        for (int i = 0; i < sigla.length(); i++) {
            this.sigla[i] = sigla.charAt(i);
        }
    }
    public String getretrospectoAsString(String separador) {
        StringJoiner joiner = new StringJoiner(separador);
        for (String valor : retrospecto) {
            joiner.add(valor);
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "Registro{" +
                "sigla='" + getSigla() + '\'' +
                ", nome='" + nome + '\'' +
                ", data=" + data +
                ", retrospecto=" + getretrospectoAsString(", ") +
                ", aproveitamento=" + aproveitamento +
                '}';
    }

    public static Registro fromBytes(byte[] data) throws IOException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
         DataInputStream dis = new DataInputStream(bais)) {

        // Check for ID
        if (dis.available() < 4) {
            throw new RuntimeException("Not enough bytes available for reading ID.");
        }
        int id = dis.readInt();

        // Check for sigla
        if (dis.available() < 2) {  // At least 2 bytes needed for UTF length info
            throw new RuntimeException("Not enough bytes available for reading sigla.");
        }
        String sigla = dis.readUTF();

        // Check for nome
        if (dis.available() < 2) {
            throw new RuntimeException("Not enough bytes available for reading nome.");
        }
        String nome = dis.readUTF();

        // Check for dataStr
        if (dis.available() < 2) {
            throw new RuntimeException("Not enough bytes available for reading dataStr.");
        }
        long epochDay = dis.readLong();
        LocalDate parsedData = LocalDate.ofEpochDay(epochDay);


        // Check for tamanhoLista
        if (dis.available() < 4) {
            throw new RuntimeException("Not enough bytes available for reading tamanhoLista.");
        }
        int tamanhoLista = dis.readInt();

        String[] retrospecto = new String[tamanhoLista];
        for (int i = 0; i < tamanhoLista; i++) {
            if (dis.available() < 2) {
                throw new RuntimeException("Not enough bytes available for reading retrospecto[" + i + "].");
            }
            retrospecto[i] = dis.readUTF();
        }

        // Check for aproveitamento
        if (dis.available() < 4) {
            throw new RuntimeException("Not enough bytes available for reading aproveitamento.");
        }
        float aproveitamento = dis.readFloat();

        return new Registro(sigla, nome, parsedData, retrospecto, aproveitamento, id);
    }
}
public byte[] toBytes() throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(baos)) {

        // Write ID
        dos.writeInt(this.id);

        // Write sigla
        dos.writeUTF(new String(this.sigla));


        // Write nome
        dos.writeUTF(this.nome);

        // Write date
        dos.writeLong(this.data.toEpochDay());

        // Write sizeRetrospecto
        dos.writeInt(this.sizeRetrospecto);

        // Write each string in retrospecto
        for (int i = 0; i < this.sizeRetrospecto; i++) {
            dos.writeUTF(this.retrospecto[i]);
        }

        // Write aproveitamento
        dos.writeFloat(this.aproveitamento);

        return baos.toByteArray();
    }
}


}


