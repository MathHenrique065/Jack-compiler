package nand2tetris.lexer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Serializes Jack tokens into the exact XML representation required by the
 * Nand2Tetris Project 10 test tools (ending with T.xml).
 */
public class TokenXmlWriter {

    /**
     * Converts the entire token stream of a JackTokenizer into an XML string.
     */
    public static String toXmlString(JackTokenizer tokenizer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tokens>\n");

        tokenizer.reset();
        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
            Token token = tokenizer.getCurrentToken();
            sb.append(token.toXmlString()).append("\n");
        }

        sb.append("</tokens>\n");
        return sb.toString();
    }

    /**
     * Writes the tokenized XML output to the specified target file.
     */
    public static void writeToFile(JackTokenizer tokenizer, Path targetPath) throws IOException {
        String xml = toXmlString(tokenizer);
        if (targetPath.getParent() != null) {
            Files.createDirectories(targetPath.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8)) {
            writer.write(xml);
        }
    }

    /**
     * Converts a raw list of tokens into an XML string.
     */
    public static String toXmlString(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tokens>\n");
        for (Token token : tokens) {
            sb.append(token.toXmlString()).append("\n");
        }
        sb.append("</tokens>\n");
        return sb.toString();
    }
}
