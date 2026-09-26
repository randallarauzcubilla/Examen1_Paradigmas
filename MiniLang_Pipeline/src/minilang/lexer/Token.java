package minilang.lexer;

/**
 * Immutable representation of a lexical token.
 *
 * <p>
 * Follows the Single Responsibility Principle: holds only token data, no
 * behavior beyond safe construction.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class Token {

    /**
     * The category of this token.
     */
    private final TokenType type;

    /**
     * The raw lexeme (string representation) from source.
     */
    private final String lexeme;

    /**
     * Source line number where the token starts (1-based).
     */
    private final int line;

    /**
     * Source column number where the token starts (1-based).
     */
    private final int column;

    /**
     * Constructs an immutable Token.
     *
     * @param type the token category (must not be null)
     * @param lexeme the raw source text (must not be null)
     * @param line source line number (1-based, positive)
     * @param column source column number (1-based, positive)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Token(final TokenType type,
            final String lexeme,
            final int line,
            final int column) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Type cannot be null");
        }
        if (lexeme == null) {
            throw new IllegalArgumentException(
                    "Lexeme cannot be null");
        }
        if (line < 1) {
            throw new IllegalArgumentException(
                    "Line must be >= 1, got: " + line);
        }
        if (column < 1) {
            throw new IllegalArgumentException(
                    "Column must be >= 1, got: " + column);
        }
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the token type.
     *
     * @return the TokenType
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the raw lexeme text.
     *
     * @return the lexeme string
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Returns the source line number (1-based).
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the source column number (1-based).
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the string representation of the token.
     *
     * @return the formatted string
     */
    @Override
    public String toString() {
        return String.format(
                "Token(%s, '%s', L%d:C%d)",
                type, lexeme, line, column);
    }
}
