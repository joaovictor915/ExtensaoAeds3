import java.io.*;
import java.util.Arrays;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class RegistroUtils {

    public static byte[] serialize(Registro registro) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(registro.getSigla());
            dos.writeUTF(registro.getNome());
            dos.writeUTF(registro.getData().toString());
            
            dos.writeInt(registro.getRetrospecto().length);
            for (String valor : registro.getRetrospecto()) {
                dos.writeUTF(valor);
            }
            
            dos.writeFloat(registro.getAproveitamento());
            return baos.toByteArray();
        }
    }

    public static Registro deserialize(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {
            String Sigla = dis.readUTF();
            String Nome = dis.readUTF();
            LocalDate dataLocal = LocalDate.parse(dis.readUTF());

            int tamanhoLista = dis.readInt();
            String[] Restrospecto = new String[tamanhoLista];
            for (int i = 0; i < tamanhoLista; i++) {
                Restrospecto[i] = (dis.readUTF());
            }
            
            float numero = dis.readFloat();

            return new Registro(Sigla, Nome, dataLocal, Restrospecto, numero);
        }
    }
}
