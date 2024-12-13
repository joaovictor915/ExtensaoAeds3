import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    // Método para importar registros de um arquivo CSV
    public List<Registro> importCSV(String fileName) {
        List<Registro> registros = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Ler cabeçalhos do CSV
            String[] headers = reader.readLine().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            // Obter índices das colunas relevantes
            int siglaIndex = indexOf(headers, "Team_Abbreviation");
            int nomeIndex = indexOf(headers, "Team_Full_Name");
            int dataIndex = indexOf(headers, "Creation_Date");
            int retrospectoIndex = indexOf(headers, "Retrospective");
            int aproveitamentoIndex = indexOf(headers, "Aproveitamento");

            String line;
            // Ler cada linha do CSV
            while ((line = reader.readLine()) != null) {
                // Dividir a linha em valores
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Extrair valores e remover aspas
                String sigla = values[siglaIndex].replace("\"", "");
                String nome = values[nomeIndex].replace("\"", "");
                LocalDate data = parseDate(values[dataIndex].replace("\"", ""));
                String[] retrospecto = values[retrospectoIndex].replace("\"", "").split(",");
                float aproveitamento = Float.parseFloat(values[aproveitamentoIndex].replace("\"", ""));

                // Criar um novo registro e adicioná-lo à lista
                Registro registro = new Registro(sigla, nome, data, retrospecto, aproveitamento);
                registros.add(registro);
            }

        } catch (Exception e) {
            System.out.println("Erro ao importar o arquivo da database: " + e.getMessage());
        }

        return registros;
    }

    // Método para encontrar o índice de um valor em um array
    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].replace("\"", "").equals(value)) {
                return i;
            }
        }
        return -1; // Não encontrado
    }

    // Método para analisar uma string de data e convertê-la em LocalDate
    private LocalDate parseDate(String dateString) {
        LocalDate data = null;

        // Se a string tem apenas o ano
        if (dateString.length() == 4) {
            data = LocalDate.of(Integer.parseInt(dateString), 1, 1);  // Define para 1º de janeiro
        } else if (dateString.chars().filter(ch -> ch == '-').count() == 2) {
            // Se a string tem o formato "yyyy-MM-dd"
            data = LocalDate.parse(dateString); // Análise padrão do LocalDate para "yyyy-MM-dd"
        } else if (dateString.chars().filter(ch -> ch == '-').count() == 1) {
            // Se a string tem o formato "yyyy-MM"
            String[] parts = dateString.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            data = LocalDate.of(year, month, 1);  // Define para o 1º dia do mês
        }

        return data;
    }
}
