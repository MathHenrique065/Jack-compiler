package nand2tetris.lexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Automated standalone validation tool for the Jack Lexical Analyzer.
 * Compares the tokenizer's XML output against official/expected reference XML files (*T.xml).
 *
 * Can be run standalone:
 *   java nand2tetris.lexer.LexerValidator
 */
public class LexerValidator {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Nand2Tetris Jack Lexical Analyzer Validator   ");
        System.out.println("=================================================\n");

        Path testDir = Paths.get("test-files");
        if (!Files.exists(testDir)) {
            System.err.println("Error: 'test-files' directory not found at " + testDir.toAbsolutePath());
            System.exit(1);
        }

        try {
            boolean allPassed = runValidation(testDir);
            System.out.println("\n-------------------------------------------------");
            if (allPassed) {
                System.out.println(" [SUCCESS] ALL TEST SUITES PASSED PERFECTLY!");
                System.out.println("-------------------------------------------------");
                System.exit(0);
            } else {
                System.out.println(" [FAILURE] SOME TESTS FAILED. See details above.");
                System.out.println("-------------------------------------------------");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Validation aborted due to error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static boolean runValidation(Path root) throws IOException {
        List<Path> jackFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".jack"))
                    .forEach(jackFiles::add);
        }

        if (jackFiles.isEmpty()) {
            System.out.println("No .jack test files found in " + root.toAbsolutePath());
            return true;
        }

        int passed = 0;
        int failed = 0;

        for (Path jackFile : jackFiles) {
            String fileName = jackFile.getFileName().toString();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            Path expectedXmlFile = jackFile.resolveSibling(baseName + "T.xml");

            if (!Files.exists(expectedXmlFile)) {
                System.out.printf("  [SKIP] %s (No reference file %s)%n", fileName, expectedXmlFile.getFileName());
                continue;
            }

            // Run tokenizer
            JackTokenizer tokenizer = new JackTokenizer(jackFile);
            String actualXml = TokenXmlWriter.toXmlString(tokenizer);
            String expectedXml = Files.readString(expectedXmlFile, StandardCharsets.UTF_8);

            ComparisonResult result = compareXml(actualXml, expectedXml);
            if (result.matches()) {
                System.out.printf("  [PASS] %-20s -> matches %-20s (%d tokens)%n",
                        fileName, expectedXmlFile.getFileName(), tokenizer.getAllTokens().size());
                passed++;
            } else {
                System.out.printf("  [FAIL] %-20s != %-20s%n", fileName, expectedXmlFile.getFileName());
                System.out.println("         First difference at line " + result.firstDiffLine() + ":");
                System.out.println("           Expected: [" + result.expectedSnippet() + "]");
                System.out.println("           Actual:   [" + result.actualSnippet() + "]");
                failed++;
            }
        }

        System.out.printf("%nSummary: %d passed, %d failed, total: %d.%n",
                passed, failed, (passed + failed));
        return failed == 0;
    }

    public record ComparisonResult(boolean matches, int firstDiffLine, String expectedSnippet, String actualSnippet) {}

    public static ComparisonResult compareXml(String actual, String expected) {
        List<String> actualLines = normalizeLines(actual);
        List<String> expectedLines = normalizeLines(expected);

        int minLen = Math.min(actualLines.size(), expectedLines.size());
        for (int i = 0; i < minLen; i++) {
            if (!actualLines.get(i).equals(expectedLines.get(i))) {
                return new ComparisonResult(false, i + 1, expectedLines.get(i), actualLines.get(i));
            }
        }

        if (actualLines.size() != expectedLines.size()) {
            int diffLine = minLen + 1;
            String expSnippet = actualLines.size() < expectedLines.size() ? expectedLines.get(minLen) : "<EOF>";
            String actSnippet = actualLines.size() > expectedLines.size() ? actualLines.get(minLen) : "<EOF>";
            return new ComparisonResult(false, diffLine, expSnippet, actSnippet);
        }

        return new ComparisonResult(true, -1, "", "");
    }

    private static List<String> normalizeLines(String text) {
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }
}
