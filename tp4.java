import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class tp4{

    // Cifra de Transposição Simples por Colunas
    public static String cifrarTransposicao(String texto, int chave) {
        texto = texto.replaceAll(" ", ""); // Remove espaços
        int coluna = chave;
        int linha = (int) Math.ceil((double) texto.length() / coluna);
        char[][] grade = new char[linha][coluna];
        StringBuilder cifrado = new StringBuilder();

        // Preenche a grade linha por linha
        int k = 0;
        for (int i = 0; i < linha; i++) {
            for (int j = 0; j < coluna; j++) {
                if (k < texto.length()) {
                    grade[i][j] = texto.charAt(k++);
                } else {
                    grade[i][j] = '-'; // Caracter de preenchimento
                }
            }
        }

        // Lê a grade coluna por coluna
        for (int j = 0; j < coluna; j++) {
            for (int i = 0; i < linha; i++) {
                cifrado.append(grade[i][j]);
            }
        }

        return cifrado.toString();
    }

    public static String decifrarTransposicao(String textoCifrado, int chave) {
        int coluna = chave;
        int linha = (int) Math.ceil((double) textoCifrado.length() / coluna);
        char[][] grade = new char[linha][coluna];
        StringBuilder decifrado = new StringBuilder();

        // Preenche a grade coluna por coluna
        int k = 0;
        for (int j = 0; j < coluna; j++) {
            for (int i = 0; i < linha; i++) {
                if (k < textoCifrado.length()) {
                    grade[i][j] = textoCifrado.charAt(k++);
                }
            }
        }

        // Lê a grade linha por linha
        for (int i = 0; i < linha; i++) {
            for (int j = 0; j < coluna; j++) {
                if (grade[i][j] != '-') {
                    decifrado.append(grade[i][j]);
                }
            }
        }

        return decifrado.toString();
    }

    // Criptografia Moderna - RSA
    public static KeyPair gerarParDeChavesRSA() throws Exception {
        KeyPairGenerator geradorChaves = KeyPairGenerator.getInstance("RSA");
        geradorChaves.initialize(2048);
        return geradorChaves.generateKeyPair();
    }

    public static String cifrarRSA(String dados, PublicKey chavePublica) throws Exception {
        Cipher cifra = Cipher.getInstance("RSA");
        cifra.init(Cipher.ENCRYPT_MODE, chavePublica);
        byte[] dadosCifrados = cifra.doFinal(dados.getBytes());
        return Base64.getEncoder().encodeToString(dadosCifrados);
    }

    public static String decifrarRSA(String dadosCifrados, PrivateKey chavePrivada) throws Exception {
        Cipher cifra = Cipher.getInstance("RSA");
        cifra.init(Cipher.DECRYPT_MODE, chavePrivada);
        byte[] dadosDecifrados = cifra.doFinal(Base64.getDecoder().decode(dadosCifrados));
        return new String(dadosDecifrados);
    }

    public static void main(String[] args) {
        try {
            // Demonstração da Cifra de Transposição por Colunas
            String textoOriginal = "HELLO WORLD";
            int chaveColuna = 5;
            String textoCifradoTransposicao = cifrarTransposicao(textoOriginal, chaveColuna);
            String textoDecifradoTransposicao = decifrarTransposicao(textoCifradoTransposicao, chaveColuna);
            System.out.println("Texto Original: " + textoOriginal);
            System.out.println("Texto Cifrado (Transposição): " + textoCifradoTransposicao);
            System.out.println("Texto Decifrado (Transposição): " + textoDecifradoTransposicao);

            // Demonstração da Criptografia RSA
            String mensagem = "Esta é uma mensagem secreta";
            KeyPair parDeChavesRSA = gerarParDeChavesRSA();
            String mensagemCifradaRSA = cifrarRSA(mensagem, parDeChavesRSA.getPublic());
            String mensagemDecifradaRSA = decifrarRSA(mensagemCifradaRSA, parDeChavesRSA.getPrivate());
            System.out.println("Mensagem Original: " + mensagem);
            System.out.println("Mensagem Cifrada (RSA): " + mensagemCifradaRSA);
            System.out.println("Mensagem Decifrada (RSA): " + mensagemDecifradaRSA);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
