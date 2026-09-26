package minilang.exception;

/**
 * Exception thrown when the Lexer encounters an invalid token
 * or malformed input in the MiniLang source file.
 *
 * <p>This exception captures the exact location (line and column)
 * of the lexical error to provide precise feedback.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public class LexerException extends RuntimeException {

    /** Line number where the error occurred. */
    private final int line;

    /** Column number where the error occurred. */
    private final int column;

    /**
     * Constructs a LexerException with error details.
     *
     * @param message description of the lexical error
     * @param line    source line number (1-based)
     * @param column  source column number (1-based)
     */
    public LexerException(final String message,
                          final int line,
                          final int column) {
        super(message);
        this.line = line;
        this.column = column;
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
     * Returns the column number where the error was found.
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the formatted error message.
     *
     * @return the formatted string
     */
    @Override
    public String getMessage() {
        return String.format(
                "[Lexer Error at line %d, col %d] %s",
                line, column, super.getMessage());
    }
}