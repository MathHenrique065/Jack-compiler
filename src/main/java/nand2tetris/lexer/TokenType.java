package nand2tetris.lexer;

/**
 * Represents the 5 lexical token types defined by the Jack language specification:
 * 1. KEYWORD: class, constructor, function, method, field, static, var, int, char,
 *             boolean, void, true, false, null, this, let, do, if, else, while, return
 * 2. SYMBOL: { } ( ) [ ] . , ; + - * / & | < > = ~
 * 3. IDENTIFIER: A sequence of letters, digits, and underscore not starting with a digit.
 * 4. INT_CONST: A decimal number in the range 0 .. 32767.
 * 5. STRING_CONST: A sequence of characters enclosed in double quotes (without newline or quote).
 */
public enum TokenType {
    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant");

    private final String xmlTagName;

    TokenType(String xmlTagName) {
        this.xmlTagName = xmlTagName;
    }

    public String getXmlTagName() {
        return xmlTagName;
    }
}
