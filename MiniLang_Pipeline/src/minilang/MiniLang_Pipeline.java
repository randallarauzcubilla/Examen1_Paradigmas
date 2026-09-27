package minilang;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import minilang.exception.LexerException;
import minilang.exception.ParserException;
import minilang.ir.IRGenerator;
import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.model.Instruction;
import minilang.parser.Parser;

/**
 * Main orchestrator for the MiniLang Pipeline.
 *
 * <p>
 * This is Stage 1 of a three-stage polyglot pipeline:</p>
 * <ol>
 * <li>Java: lexing, parsing, OOP modeling, IR gen</li>
 * <li>Python: functional execution of operations</li>
 * <li>MIPS: checksum and signature verification</li>
 * </ol>
 *
 * <p>
 * This class reads a MiniLang source file, validates it, and produces an
 * intermediate representation file. It contains no hardcoded paths; all file
 * locations are resolved relative to the working directory.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class MiniLang_Pipeline {

    /**
     * Default input file name.
     */
    private static final String INPUT_FILE = "programa.mini";

    /**
     * Default output IR file name (deterministic path to output folder).
     */
    private static final String OUTPUT_FILE = "output/programa.ir";

    /**
     * Private constructor to prevent instantiation. This class serves only as
     * an entry point.
     *
     * @throws UnsupportedOperationException always
     */
    private MiniLang_Pipeline() {
        throw new UnsupportedOperationException(
                "Entry point class cannot be "
                + "instantiated");
    }

    /**
     * Application entry point.
     *
     * <p>
     * Accepts an optional argument for the input file path. If not provided,
     * defaults to "programa.mini".</p>
     *
     * @param args command line arguments (optional: input)
     */
    public static void main(final String[] args) {
        // Accept input file from args, or use default
        final String inputPath
                = (args.length > 0) ? args[0] : INPUT_FILE;

        // Accept output file from args, or use default
        final String outputPath
                = (args.length > 1) ? args[1] : OUTPUT_FILE;

        System.out.println(
                "=== MiniLang Pipeline - Stage 1 (Java) ===");
        System.out.println("Reading: " + inputPath);

        try {
            final List<String> lines = readFile(inputPath);

            // Stage 1a: Lexical analysis
            final Lexer lexer = new Lexer(lines);
            final List<List<Token>> tokens = lexer.tokenize();
            System.out.println(
                    "[OK] Lexer: " + countTokens(tokens)
                    + " tokens found");

            // Stage 1b: Syntactic analysis
            final Parser parser = new Parser(tokens);
            final List<Instruction> instructions
                    = parser.parse();
            System.out.println(
                    "[OK] Parser: " + instructions.size()
                    + " instructions validated");

            // Stage 1c: IR generation (polymorphism)
            final String irContent
                    = IRGenerator.generate(instructions);
            writeFile(outputPath, irContent);
            System.out.println(
                    "[OK] IR written to: " + outputPath);

            System.out.println(
                    "=== Stage 1 completed successfully ===");

        } catch (LexerException e) {
            System.err.println(
                    "LEXER ERROR: " + e.getMessage());
            System.exit(1);
        } catch (ParserException e) {
            System.err.println(
                    "PARSER ERROR: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(
                    "FILE ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Reads all lines from the specified file.
     *
     * <p>
     * Implements fallback path resolution: if the file is not found at the
     * given path, it searches common project locations. This makes the pipeline
     * robust against different working directory configurations.</p>
     *
     * @param path the file path to read
     * @return the list of lines
     * @throws IOException if the file cannot be found anywhere
     */
    private static List<String> readFile(final String path) throws IOException {
        Path filePath = Paths.get(path).toAbsolutePath().normalize();
        if (!Files.exists(filePath)) {
            throw new IOException("Input file not found: " + filePath);
        }
        return Files.readAllLines(filePath, StandardCharsets.UTF_8);
    }

    /**
     * Writes content to the specified file.
     *
     * <p>
     * If the path is a simple filename (no directory), the file is written to
     * the "output" directory. This ensures all generated artifacts are
     * centralized.</p>
     *
     * @param path the output file path
     * @param content the content to write
     * @throws IOException if the file cannot be written
     */
    private static void writeFile(final String path, final String content) throws IOException {
        // ESCRITURA ATÓMICA: evita dejar un .ir corrupto o viejo si falla a mitad
        Path targetPath = Paths.get(path).toAbsolutePath().normalize();
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");

        Files.write(tempPath, content.getBytes(StandardCharsets.UTF_8));
        Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Counts the total number of tokens across all lines.
     *
     * @param tokenLines the list of token lists
     * @return the total token count
     */
    private static int countTokens(
            final List<List<Token>> tokenLines) {
        int count = 0;
        for (List<Token> line : tokenLines) {
            count += line.size();
        }
        return count;
    }
}
