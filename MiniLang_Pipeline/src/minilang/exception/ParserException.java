package minilang.exception;

/**
 * Exception thrown when the Parser encounters a syntactic error
 * that violates the MiniLang grammar rules.
 *
 * <p>This exception captures the line number where the syntax
 * error occurred, enabling clear error reporting.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public class ParserException extends RuntimeException {

    /** Line number where the syntax error occurred. */
    private final int line;

    /**
     * Constructs a ParserException with error details.
     *
     * @param message description of the syntax error
     * @param line    source line number (1-based)
     */
    public ParserException(final String message, final int line) {
        super(message);
        this.line = line;
    }

    /**
     * Returns the line number where the error was found.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the formatted error message.
     *
     * @return the formatted string
     */
    @Override
    public String getMessage() {
        return String.format(
                "[Parser Error at line %d] %s",
                line, super.getMessage());
    }
}