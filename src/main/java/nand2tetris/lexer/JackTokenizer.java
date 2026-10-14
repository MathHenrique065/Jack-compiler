package nand2tetris.lexer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Lexical analyzer (tokenizer/scanner) for the Jack programming language.
 * Removes all comments and whitespace from the input stream and breaks it
 * into individual Jack tokens as specified in Nand2Tetris Project 10.
 */
public class JackTokenizer {

    public static final Set<String> KEYWORDS = Set.of(
            "class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this",
            "let", "do", "if", "else", "while", "return"
    );

    public static final Set<Character> SYMBOLS = Set.of(
            '{', '}', '(', ')', '[', ']', '.', ',', ';',
            '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'
    );

    private final List<Token> tokens;
    private int currentIndex;
    private Token currentToken;

    /**
     * Initializes the tokenizer from a source code string.
     */
    public JackTokenizer(String sourceCode) {
        this.tokens = tokenize(sourceCode);
        this.currentIndex = -1;
        this.currentToken = null;
    }

    /**
     * Initializes the tokenizer from a Path.
     */
    public JackTokenizer(Path filePath) throws IOException {
        this(Files.readString(filePath, StandardCharsets.UTF_8));
    }

    /**
     * Initializes the tokenizer from a File.
     */
    public JackTokenizer(File file) throws IOException {
        this(file.toPath());
    }

    /**
     * Tokenizes raw Jack source code into an ordered list of Token instances.
     */
    private static List<Token> tokenize(String source) {
        List<Token> tokenList = new ArrayList<>();
        int len = source.length();
        int i = 0;
        int line = 1;

        while (i < len) {
            char c = source.charAt(i);

            // Track line numbers
            if (c == '\n') {
                line++;
                i++;
                continue;
            }

            // Skip whitespaces
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Check for comments: // and /* ... */ and /** ... */
            if (c == '/' && (i + 1 < len)) {
                char next = source.charAt(i + 1);
                if (next == '/') {
                    // Line comment: skip until newline or end of file
                    i += 2;
                    while (i < len && source.charAt(i) != '\n') {
                        i++;
                    }
                    continue;
                } else if (next == '*') {
                    // Block comment / Javadoc: skip until */ or end of file
                    i += 2;
                    while (i + 1 < len && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/')) {
                        if (source.charAt(i) == '\n') {
                            line++;
                        }
                        i++;
                    }
                    if (i + 1 < len) {
                        i += 2; // Skip closing */
                    } else {
                        i = len; // Unterminated comment at EOF
                    }
                    continue;
                }
            }

            // Symbol token
            if (SYMBOLS.contains(c)) {
                tokenList.add(new Token(TokenType.SYMBOL, String.valueOf(c), line));
                i++;
                continue;
            }

            // String constant: "..."
            if (c == '"') {
                i++; // Skip opening quote
                int start = i;
                while (i < len && source.charAt(i) != '"' && source.charAt(i) != '\n') {
                    i++;
                }
                String strVal = source.substring(start, i);
                if (i < len && source.charAt(i) == '"') {
                    i++; // Skip closing quote
                }
                tokenList.add(new Token(TokenType.STRING_CONST, strVal, line));
                continue;
            }

            // Integer constant: 0 .. 32767
            if (Character.isDigit(c)) {
                int start = i;
                while (i < len && Character.isDigit(source.charAt(i))) {
                    i++;
                }
                String numStr = source.substring(start, i);
                if (numStr.length() > 5 || Integer.parseInt(numStr) > 32767) {
                    throw new IllegalArgumentException(
                            String.format("Integer constant '%s' out of range 0..32767 at line %d", numStr, line)
                    );
                }
                tokenList.add(new Token(TokenType.INT_CONST, numStr, line));
                continue;
            }

            // Identifier or Keyword: starts with letter or '_'
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < len && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_')) {
                    i++;
                }
                String word = source.substring(start, i);
                if (KEYWORDS.contains(word)) {
                    tokenList.add(new Token(TokenType.KEYWORD, word, line));
                } else {
                    tokenList.add(new Token(TokenType.IDENTIFIER, word, line));
                }
                continue;
            }

            throw new IllegalArgumentException(
                    String.format("Invalid character '%c' (ASCII %d) at line %d", c, (int) c, line)
            );
        }

        return tokenList;
    }

    /**
     * Do we have more tokens in the input?
     */
    public boolean hasMoreTokens() {
        return currentIndex + 1 < tokens.size();
    }

    /**
     * Gets the next token from the input and makes it the current token.
     * This method should only be called if {@link #hasMoreTokens()} is true.
     */
    public void advance() {
        if (!hasMoreTokens()) {
            throw new IllegalStateException("No more tokens available.");
        }
        currentIndex++;
        currentToken = tokens.get(currentIndex);
    }

    /**
     * Returns the type of the current token.
     */
    public TokenType tokenType() {
        ensureCurrentToken();
        return currentToken.getType();
    }

    /**
     * Returns the keyword which is the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#KEYWORD}.
     */
    public String keyWord() {
        ensureType(TokenType.KEYWORD);
        return currentToken.getValue();
    }

    /**
     * Returns the character which is the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#SYMBOL}.
     */
    public char symbol() {
        ensureType(TokenType.SYMBOL);
        return currentToken.getValue().charAt(0);
    }

    /**
     * Returns the identifier which is the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#IDENTIFIER}.
     */
    public String identifier() {
        ensureType(TokenType.IDENTIFIER);
        return currentToken.getValue();
    }

    /**
     * Returns the integer value of the current token.
     * Should be called only when {@link #tokenType()} is {@link TokenType#INT_CONST}.
     */
    public int intVal() {
        ensureType(TokenType.INT_CONST);
        return Integer.parseInt(currentToken.getValue());
    }

    /**
     * Returns the string value of the current token, without the double quotes.
     * Should be called only when {@link #tokenType()} is {@link TokenType#STRING_CONST}.
     */
    public String stringVal() {
        ensureType(TokenType.STRING_CONST);
        return currentToken.getValue();
    }

    /**
     * Returns the currently active Token object.
     */
    public Token getCurrentToken() {
        ensureCurrentToken();
        return currentToken;
    }

    /**
     * Returns an unmodifiable list of all parsed tokens.
     */
    public List<Token> getAllTokens() {
        return Collections.unmodifiableList(tokens);
    }

    /**
     * Resets the tokenizer back to the beginning of the token stream.
     */
    public void reset() {
        currentIndex = -1;
        currentToken = null;
    }

    private void ensureCurrentToken() {
        if (currentToken == null) {
            throw new IllegalStateException("advance() must be called at least once before inspecting tokens.");
        }
    }

    private void ensureType(TokenType expected) {
        ensureCurrentToken();
        if (currentToken.getType() != expected) {
            throw new IllegalStateException(
                    String.format("Expected token type %s but was %s ('%s') at line %d",
                            expected, currentToken.getType(), currentToken.getValue(), currentToken.getLineNumber())
            );
        }
    }
}
