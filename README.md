# README

## Descrição do Programa

Este é um programa em Java para gerenciamento de registros em arquivos binários, com suporte a operações como criação, leitura, atualização e exclusão de registros. Também inclui funcionalidades de ordenação externa e índices baseados em árvores B e hashing.

---

## Funcionalidades Existentes
1. **Carregar base de dados**: Importa registros de um arquivo CSV e os salva em um arquivo binário.
2. **Ler um registro (ID)**: Busca um registro pelo ID e exibe suas informações.
3. **Atualizar um registro**: Modifica os dados de um registro existente com base em seu ID.
4. **Deletar um registro (ID)**: Remove um registro identificado pelo ID.
5. **Sair**: Encerra o programa.
6. **Ordenação externa (método intercalado balanceado)**: Ordena registros em arquivos utilizando o método intercalado balanceado.
7. **Ordenação externa comum**: Variante da ordenação externa.
8. **Indexação por árvore B**: Cria índices e realiza buscas em registros utilizando árvores B.
9. **Indexação por hashing**: Cria índices e realiza buscas em registros utilizando hashing.
10. **Exclusão com árvore B**: Remove registros utilizando árvores B.
11. **Criação com árvore B**: Adiciona novos registros utilizando árvores B.
12. **Atualização com árvore B**: Atualiza registros utilizando árvores B.
13. **Exclusão com hashing**: Remove registros utilizando hashing.
14. **Criação com hashing**: Adiciona novos registros utilizando hashing.
15. **Atualização com hashing**: Atualiza registros utilizando hashing.
16. **Ordenação com segmentos de tamanho variável**: Realiza ordenação externa com segmentos de tamanho variável.

---

## Como Usar

1. **Configurar o ambiente**:
   - Certifique-se de ter o Java 8 ou superior instalado.
   - Compile o programa com o comando: `javac Main.java`.

2. **Executar o programa**:
   - Use o comando: `java Main`.
   - Siga as instruções do menu para escolher a funcionalidade desejada.

3. **Entradas**:
   - O programa solicita informações como caminhos de arquivos, IDs de registros e dados específicos para cada operação.

4. **Saídas**:
   - As saídas são exibidas no console, incluindo mensagens de erro ou confirmação de operações bem-sucedidas.

---

## Nova Funcionalidade: Criptografia de Registros

### Descrição
Implementar a funcionalidade de criptografia para proteger os registros armazenados no arquivo binário. O usuário pode optar por criptografar ou descriptografar o arquivo completo, garantindo a segurança dos dados.

### Como Funciona
- **Opção 17: Criptografia de Arquivos**:
  1. O usuário escolhe um arquivo binário existente.
  2. É solicitada uma chave de criptografia (senha).
  3. Os dados são criptografados/descriptografados usando um algoritmo como AES (Advanced Encryption Standard).

### Exemplo de Interação no Console
```
Digite o nome do arquivo para criptografar/descriptografar: database.bin
Escolha: 1 para criptografar ou 2 para descriptografar
Digite a chave de criptografia: ****
Operação concluída com sucesso!
```

### Benefícios
- Garante que os dados armazenados não sejam acessados sem autorização.
- Facilita a conformidade com padrões de segurança.

