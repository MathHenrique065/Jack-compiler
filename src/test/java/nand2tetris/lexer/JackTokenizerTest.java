package nand2tetris.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JackTokenizerTest {

    @Test
    @DisplayName("Should correctly recognize all 21 Jack keywords")
    void testKeywords() {
        String source = "class constructor function method field static var int char boolean void true false null this let do if else while return";
        JackTokenizer tokenizer = new JackTokenizer(source);

        List<Token> tokens = tokenizer.getAllTokens();
        assertEquals(21, tokens.size());

        for (Token token : tokens) {
            assertEquals(TokenType.KEYWORD, token.getType());
            assertTrue(JackTokenizer.KEYWORDS.contains(token.getValue()));
        }
    }

    @Test
    @DisplayName("Should correctly recognize all 19 Jack symbols and escape XML entities")
    void testSymbolsAndEscaping() {
        String source = "{ } ( ) [ ] . , ; + - * / & | < > = ~";
        JackTokenizer tokenizer = new JackTokenizer(source);

        List<Token> tokens = tokenizer.getAllTokens();
        assertEquals(19, tokens.size());

        for (Token token : tokens) {
            assertEquals(TokenType.SYMBOL, token.getType());
        }

        // Test XML escaping
        Token lessThan = tokens.stream().filter(t -> t.getValue().equals("<")).findFirst().orElseThrow();
        assertEquals("<symbol> &lt; </symbol>", lessThan.toXmlString());

        Token greaterThan = tokens.stream().filter(t -> t.getValue().equals(">")).findFirst().orElseThrow();
        assertEquals("<symbol> &gt; </symbol>", greaterThan.toXmlString());

        Token ampersand = tokens.stream().filter(t -> t.getValue().equals("&")).findFirst().orElseThrow();
        assertEquals("<symbol> &amp; </symbol>", ampersand.toXmlString());
    }

    @Test
    @DisplayName("Should ignore line, block, and javadoc comments")
    void testCommentsIgnored() {
        String source = """
                // Single line comment
                class Main {
                    /* Block comment
                       spanning multiple lines */
                    /** Javadoc comment */
                    function void main() {
                        return; // Trailing comment
                    }
                }
                """;

        JackTokenizer tokenizer = new JackTokenizer(source);
        List<Token> tokens = tokenizer.getAllTokens();

        // Tokens should be: class Main { function void main ( ) { return ; } }
        List<String> expectedValues = List.of(
                "class", "Main", "{", "function", "void", "main", "(", ")", "{", "return", ";", "}", "}"
        );

        List<String> actualValues = tokens.stream().map(Token::getValue).toList();
        assertEquals(expectedValues, actualValues);
    }

    @Test
    @DisplayName("Should distinguish division symbol '/' from comments")
    void testDivisionSymbolVsComment() {
        String source = "let x = sum / count; // division test";
        JackTokenizer tokenizer = new JackTokenizer(source);

        List<String> values = tokenizer.getAllTokens().stream().map(Token::getValue).toList();
        assertEquals(List.of("let", "x", "=", "sum", "/", "count", ";"), values);
    }

    @Test
    @DisplayName("Should properly parse String Constants with spaces and preserve content")
    void testStringConstant() {
        String source = "let s = \"HOW MANY NUMBERS? \";";
        JackTokenizer tokenizer = new JackTokenizer(source);

        assertTrue(tokenizer.hasMoreTokens());
        tokenizer.advance(); // let
        tokenizer.advance(); // s
        tokenizer.advance(); // =
        tokenizer.advance(); // string constant

        assertEquals(TokenType.STRING_CONST, tokenizer.tokenType());
        assertEquals("HOW MANY NUMBERS? ", tokenizer.stringVal());
        assertEquals("<stringConstant> HOW MANY NUMBERS? </stringConstant>", tokenizer.getCurrentToken().toXmlString());
    }

    @Test
    @DisplayName("Should properly parse Integer Constants within 0..32767")
    void testIntegerConstants() {
        String source = "let a = 0; let b = 32767;";
        JackTokenizer tokenizer = new JackTokenizer(source);

        tokenizer.advance(); // let
        tokenizer.advance(); // a
        tokenizer.advance(); // =
        tokenizer.advance(); // 0
        assertEquals(TokenType.INT_CONST, tokenizer.tokenType());
        assertEquals(0, tokenizer.intVal());

        tokenizer.advance(); // ;
        tokenizer.advance(); // let
        tokenizer.advance(); // b
        tokenizer.advance(); // =
        tokenizer.advance(); // 32767
        assertEquals(TokenType.INT_CONST, tokenizer.tokenType());
        assertEquals(32767, tokenizer.intVal());
    }

    @Test
    @DisplayName("Should reject integer constants above 32767 but accept leading zeros")
    void testIntegerRangeValidation() {
        assertThrows(IllegalArgumentException.class, () -> new JackTokenizer("let a = 32768;"));
        assertThrows(IllegalArgumentException.class, () -> new JackTokenizer("let a = 99999999999999999999;"));
        assertEquals(1, new JackTokenizer("000001").getAllTokens().size());
    }

    @Test
    @DisplayName("Should fail on unterminated block comments")
    void testUnterminatedBlockComment() {
        assertThrows(IllegalArgumentException.class, () -> new JackTokenizer("class Main { /* never closed"));
    }

    @Test
    @DisplayName("Should validate against test-files/ if present")
    void testReferenceFilesIfPresent() throws IOException {
        Path testDir = Paths.get("test-files");
        if (java.nio.file.Files.exists(testDir)) {
            boolean matches = LexerValidator.runValidation(testDir);
            assertTrue(matches, "Tokenizer output should match all reference XML files");
        }
    }
}
