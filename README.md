# Compilador Jack - Analisador Léxico + Sintático (Nand2Tetris - Projeto 10)

Repositório unificado: analisador léxico (`nand2tetris.lexer`) e analisador sintático (`nand2tetris.parser`), usados em conjunto. Base para as próximas unidades (Compilation Engine, Code Generator).

Implementação completa em **Java** do analisador léxico (*scanner / tokenizer*) para a linguagem **Jack**, especificada no Capítulo 10 do livro *The Elements of Computing Systems* (Nand2Tetris).

## 👥 Aluno

| Nome | Matrícula |
|------|-----------|
| Matheus Henrique Albuquerque Torres | 2020002089 |

**Repositório:** https://github.com/MathHenrique065/Jack-compiler

**Linguagem utilizada:** Java 17+ (Maven opcional; scripts `.bat` usam apenas o JDK).

---

## 🧪 Estratégia de Testes

Adotamos a estratégia **XML padrão do nand2tetris**, com duas camadas:

1. **Validação contra os gabaritos oficiais (`*T.xml`)** – `LexerValidator` (`test.bat`) gera o XML de `Main.jack`, `Square.jack` e `SquareGame.jack` e compara **byte a byte** com `MainT.xml`, `SquareT.xml` e `SquareGameT.xml`. O formato segue o padrão: `<tokens>`…`</tokens>`, tags com espaços internos (`<symbol> ; </symbol>`), escapes `&lt; &gt; &quot; &amp;`, strings sem aspas e **sem token EOF**.
2. **Testes unitários JUnit 5** (`JackTokenizerTest`, `mvn test`) – cobrem as 5 categorias, os 21 keywords, os 19 símbolos, `/` vs. comentários (`//`, `/* */`, `/** */`), contagem de linhas, e erros léxicos (inteiro > 32767, comentário não fechado, caractere inválido).

Como isso garante corretude e cobertura: os arquivos oficiais exercitam todas as categorias de token e todos os tipos de comentário; a comparação exata detecta qualquer diferença de token, ordem ou formatação; os testes unitários cobrem casos de borda que os arquivos oficiais não contêm.

---

## 📁 Estrutura do Projeto

```text
Jack-compiler/
├── src/
│   ├── main/java/nand2tetris/lexer/
│   │   ├── TokenType.java         # Enum com os 5 tipos de tokens do Jack
│   │   ├── Token.java             # Modelo de token individual com serialização XML
│   │   ├── JackTokenizer.java     # Motor de análise léxica (API padrão Nand2Tetris)
│   │   ├── TokenXmlWriter.java    # Escritor e formatador do arquivo de saída *T.xml
│   │   ├── JackAnalyzer.java      # Ponto de entrada CLI (analisa arquivo ou pasta)
│   │   └── LexerValidator.java    # Validador automático independente
│   └── test/java/nand2tetris/lexer/
│       └── JackTokenizerTest.java # Suíte de testes unitários (JUnit 5)
│
├── test-files/                    # Arquivos de teste oficiais do Nand2Tetris
│   ├── ArrayTest/
│   │   ├── Main.jack
│   │   └── MainT.xml              # Gabarito oficial de tokens
│   └── Square/
│       ├── Main.jack
│       ├── MainT.xml
│       ├── Square.jack
│       ├── SquareT.xml
│       ├── SquareGame.jack
│       └── SquareGameT.xml
│
├── build.bat                      # Script rápido para compilar (sem precisar de Maven)
├── run.bat                        # Script rápido para executar o analisador
├── test.bat                       # Script rápido para executar a validação de testes
├── pom.xml                        # Configuração padrão do Maven
└── README.md
```

---

## 🎯 Elementos Léxicos da Linguagem Jack

O analisador processa o código `.jack`, elimina comentários e espaços em branco, e classifica cada token em uma de 5 categorias:

1. **`keyword`**: As 21 palavras-chave reservadas:  
   `class`, `constructor`, `function`, `method`, `field`, `static`, `var`, `int`, `char`, `boolean`, `void`, `true`, `false`, `null`, `this`, `let`, `do`, `if`, `else`, `while`, `return`.
2. **`symbol`**: Os 19 símbolos e operadores:  
   `{`, `}`, `(`, `)`, `[`, `]`, `.`, `,`, `;`, `+`, `-`, `*`, `/`, `&`, `|`, `<`, `>`, `=`, `~`.  
   *Nota: na saída XML, os caracteres reservados `<` (`&lt;`), `>` (`&gt;`), `"` (`&quot;`) e `&` (`&amp;`) são devidamente escapados.*
3. **`integerConstant`**: Números decimais no intervalo de `0` a `32767`.
4. **`stringConstant`**: Cadeia de caracteres delimitada por aspas duplas `"..."` (sem as aspas no XML final).
5. **`identifier`**: Nomes de variáveis, classes e sub-rotinas (iniciando com letra ou `_`, seguidos por letras, dígitos ou `_`).

### Tratamento de Comentários e Ruídos:
* Comentários de linha única: `// comentário até o fim da linha`
* Comentários de bloco: `/* comentário */`
* Comentários de documentação (Javadoc): `/** comentário de API */`
* O operador de divisão `/` é corretamente diferenciado de inícios de comentários (`//` ou `/*`).

### Erros Léxicos:
* Constante inteira fora do intervalo `0..32767` lança `IllegalArgumentException` (com o número da linha). Zeros à esquerda (ex.: `000001`) são aceitos.
* Comentário de bloco não fechado (`/* ...` sem `*/`) lança `IllegalArgumentException`.
* Caractere inválido também lança `IllegalArgumentException`.

---

## 🚀 Como Executar

### Opção 1: Usando os Scripts Batch (Mais rápido, apenas requer JDK 17+)

No terminal (Prompt de Comando `cmd.exe`):

1. **Compilar o projeto:**
   ```cmd
   build.bat
   ```

2. **Executar a validação automática contra os gabaritos oficiais:**
   ```cmd
   test.bat
   ```

3. **Analisar um arquivo ou diretório `.jack` qualquer:**
   ```cmd
   run.bat test-files\Square
   ```
   *Ou um arquivo específico:*
   ```cmd
   run.bat test-files\ArrayTest\Main.jack
   ```

---

### Opção 2: Usando o Maven

```cmd
mvn compile
mvn test
mvn package
```

Para rodar o JAR gerado:
```cmd
java -jar target/jack-compiler-lexical-1.0.0.jar test-files/Square
```

---

### Opção 3: Usando `javac` e `java` diretamente

```cmd
javac -encoding UTF-8 -d target/classes src/main/java/nand2tetris/lexer/*.java
java -Dfile.encoding=UTF-8 -cp target/classes nand2tetris.lexer.LexerValidator
java -Dfile.encoding=UTF-8 -cp target/classes nand2tetris.lexer.JackAnalyzer test-files/Square
```

---

---

## Analisador Sintatico (Projeto 10 - parser)

O parser (`nand2tetris.parser.Parser`) consome os tokens produzidos pelo `JackTokenizer` do analisador lexico (um unico tokenizer para todo o projeto) e gera a arvore sintatica em XML.

```
src/main/java/nand2tetris/lexer/   scanner (JackTokenizer, Token, TokenType, ...)
src/main/java/nand2tetris/parser/  parser (Parser, JackCompiler = scanner + parser, Validate)
```

Compilar e executar (gera `Nome.xml` e `NomeT.xml`):

```
javac -d out (Get-ChildItem -Recurse src/main -Filter *.java).FullName
java -cp out nand2tetris.parser.JackCompiler <Arquivo.jack | pasta> [saida]
java -cp out nand2tetris.parser.Validate <gerado> <referencia>
```
