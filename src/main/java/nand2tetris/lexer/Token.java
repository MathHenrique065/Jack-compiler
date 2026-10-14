package nand2tetris.lexer;

import java.util.Objects;

/**
 * Represents an individual token parsed from Jack source code,
 * retaining its type, raw text value, and the line number where it occurred.
 */
public class Token {
    private final TokenType type;
    private final String value;
    private final int lineNumber;

    public Token(TokenType type, String value, int lineNumber) {
        this.type = Objects.requireNonNull(type, "TokenType cannot be null");
        this.value = Objects.requireNonNull(value, "Token value cannot be null");
        this.lineNumber = lineNumber;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Formats this token as an XML element adhering to Nand2Tetris specifications:
     * {@code <tokenType> value </tokenType>}
     * Special XML characters (&lt;, &gt;, &quot;, &amp;) are escaped for symbol tokens.
     */
    public String toXmlString() {
        String escapedValue = escapeXml(value);
        // Tentativa de corrigir espacamento duplicado em stringConstant
        if (type == TokenType.STRING_CONST && escapedValue.endsWith(" ")) {
            return String.format("<%s> %s</%s>", type.getXmlTagName(), escapedValue.trim(), type.getXmlTagName());
        }
        return String.format("<%s> %s </%s>", type.getXmlTagName(), escapedValue, type.getXmlTagName());
    }

    private String escapeXml(String text) {
        if (type == TokenType.SYMBOL) {
            return switch (text) {
                case "<" -> "&lt;";
                case ">" -> "&gt;";
                case "&" -> "&amp;";
                case "\"" -> "&quot;";
                default -> text;
            };
        }
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;
        return type == token.type && Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return String.format("Token{%s, '%s', line %d}", type, value, lineNumber);
    }
}
