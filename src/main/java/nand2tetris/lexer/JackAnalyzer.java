package nand2tetris.lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Main entry point for the Jack Lexical Analyzer CLI.
 * Usage:
 *   java nand2tetris.lexer.JackAnalyzer <source-file.jack | source-directory>
 *
 * For each input .jack file, produces a corresponding [FileName]T.xml file
 * in the same directory.
 */
public class JackAnalyzer {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("=== Jack Lexical Analyzer (Nand2Tetris Project 10) ===");
            System.out.println("Usage: java nand2tetris.lexer.JackAnalyzer <source.jack | source-directory>");
            System.out.println("Defaulting to scanning 'test-files/' directory if present...\n");

            Path defaultDir = Paths.get("test-files");
            if (Files.exists(defaultDir)) {
                processPath(defaultDir);
            } else {
                System.err.println("Error: No input path provided and 'test-files/' directory not found.");
                System.exit(1);
            }
            return;
        }

        Path targetPath = Paths.get(args[0]);
        if (!Files.exists(targetPath)) {
            System.err.println("Error: Path not found: " + targetPath.toAbsolutePath());
            System.exit(1);
        }

        processPath(targetPath);
    }

    public static void processPath(Path targetPath) {
        try {
            List<Path> jackFiles = findJackFiles(targetPath);
            if (jackFiles.isEmpty()) {
                System.out.println("No .jack files found in: " + targetPath.toAbsolutePath());
                return;
            }

            System.out.printf("Found %d .jack file(s) to tokenize in: %s%n%n",
                    jackFiles.size(), targetPath.toAbsolutePath());

            int processedCount = 0;
            for (Path jackFile : jackFiles) {
                processSingleFile(jackFile);
                processedCount++;
            }

            System.out.printf("%nSuccessfully processed %d file(s).%n", processedCount);

        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processSingleFile(Path jackFile) throws IOException {
        String fileName = jackFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        Path outputFile = jackFile.resolveSibling(baseName + "T.xml");

        JackTokenizer tokenizer = new JackTokenizer(jackFile);
        int tokenCount = tokenizer.getAllTokens().size();

        TokenXmlWriter.writeToFile(tokenizer, outputFile);

        System.out.printf("  [OK] %-25s -> %-25s (%d tokens)%n",
                fileName, outputFile.getFileName(), tokenCount);
    }

    private static List<Path> findJackFiles(Path root) throws IOException {
        List<Path> jackFiles = new ArrayList<>();
        if (Files.isRegularFile(root)) {
            if (root.getFileName().toString().endsWith(".jack")) {
                jackFiles.add(root);
            }
        } else if (Files.isDirectory(root)) {
            try (Stream<Path> stream = Files.walk(root)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".jack"))
                        .forEach(jackFiles::add);
            }
        }
        return jackFiles;
    }
}
