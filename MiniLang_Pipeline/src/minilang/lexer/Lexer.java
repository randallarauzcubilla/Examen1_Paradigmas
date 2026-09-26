package minilang.lexer;

import java.util.ArrayList;
import java.util.List;

import minilang.exception.LexerException;

/**
 * Lexical analyzer for the MiniLang language.
 *
 * <p>
 * This class reads raw source lines and converts them into structured tokens.
 * It follows the Single Responsibility Principle: it only handles tokenization,
 * not grammar validation (that is the Parser's job).</p>
 *
 * <p>
 * Recognized token categories:</p>
 * <ul>
 * <li>Keywords: DATA, FILTER, MAP, REDUCE, PRINT</li>
 * <li>Reduce operators: SUM, MAX, MIN, AVG (extension)</li>
 * <li>Comparison: &gt; &lt; &gt;= &lt;= ==</li>
 * <li>Arithmetic: + - * / (extension)</li>
 * <li>Non-negative integer literals</li>
 * </ul>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class Lexer {

    /**
     * Source lines to tokenize.
     */
    private final List<String> lines;

    /**
     * Constructs a Lexer with the source lines.
     *
     * @param lines the source lines (must not be null)
     * @throws IllegalArgumentException if lines is null
     */
    public Lexer(final List<String> lines) {
        if (lines == null) {
            throw new IllegalArgumentException(
                    "Source lines cannot be null");
        }
        this.lines = new ArrayList<>(lines);
    }

    /**
     * Tokenizes all source lines.
     *
     * <p>
     * Each source line produces a list of tokens. Empty or blank lines are
     * skipped silently.</p>
     *
     * @return a list of token lists (one per non-empty line)
     * @throws LexerException if an invalid character is found
     */
    public List<List<Token>> tokenize() {
        final List<List<Token>> result = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            final int lineNum = i + 1;
            final String line = lines.get(i);
            final List<Token> tokens
                    = tokenizeLine(line, lineNum);
            if (!tokens.isEmpty()) {
                result.add(tokens);
            }
        }
        return result;
    }

    /**
     * Tokenizes a single source line.
     *
     * @param line the raw source line
     * @param lineNum the 1-based line number
     * @return the list of tokens found in this line
     * @throws LexerException if an invalid character is found
     */
    private List<Token> tokenizeLine(final String line,
            final int lineNum) {
        final List<Token> tokens = new ArrayList<>();
        final int[] pos = {0};

        while (pos[0] < line.length()) {
            skipWhitespace(line, pos);
            if (pos[0] >= line.length()) {
                break;
            }
            final Token token
                    = readToken(line, pos, lineNum);
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * Skips whitespace characters starting at pos.
     *
     * @param line the source line
     * @param pos mutable position array (index 0)
     */
    private void skipWhitespace(final String line,
            final int[] pos) {
        while (pos[0] < line.length()
                && Character.isWhitespace(
                        line.charAt(pos[0]))) {
            pos[0]++;
        }
    }

    /**
     * Reads the next token starting at the current position.
     *
     * @param line the source line
     * @param pos mutable position array (index 0)
     * @param lineNum the 1-based line number
     * @return the recognized token
     * @throws LexerException if no valid token is found
     */
    private Token readToken(final String line,
            final int[] pos,
            final int lineNum) {
        final int col = pos[0] + 1;
        final char c = line.charAt(pos[0]);

        if (Character.isDigit(c)) {
            return readNumber(line, pos, lineNum, col);
        }
        if (Character.isLetter(c)) {
            return readWord(line, pos, lineNum, col);
        }
        if (isOperatorStart(c)) {
            return readOperator(line, pos, lineNum, col);
        }

        throw new LexerException(
                "Unexpected character: '" + c + "'",
                lineNum, col);
    }

    /**
     * Reads a non-negative integer literal.
     *
     * @param line the source line
     * @param pos mutable position array
     * @param lineNum the 1-based line number
     * @param col the 1-based column number
     * @return an INTEGER token
     */
    private Token readNumber(final String line,
            final int[] pos,
            final int lineNum,
            final int col) {
        final int start = pos[0];
        while (pos[0] < line.length()
                && Character.isDigit(
                        line.charAt(pos[0]))) {
            pos[0]++;
        }
        final String lexeme
                = line.substring(start, pos[0]);
        return new Token(
                TokenType.INTEGER, lexeme, lineNum, col);
    }

    /**
     * Reads a keyword or reduce operator word.
     *
     * <p>
     * Recognized words are mapped to their token types. Unknown words produce a
     * LexerException.</p>
     *
     * @param line the source line
     * @param pos mutable position array
     * @param lineNum the 1-based line number
     * @param col the 1-based column number
     * @return the keyword or reduce-operator token
     * @throws LexerException if the word is not recognized
     */
    private Token readWord(final String line,
            final int[] pos,
            final int lineNum,
            final int col) {
        final int start = pos[0];
        while (pos[0] < line.length()
                && Character.isLetter(
                        line.charAt(pos[0]))) {
            pos[0]++;
        }
        final String lexeme
                = line.substring(start, pos[0]);
        final TokenType type = classifyWord(lexeme);
        if (type == null) {
            throw new LexerException(
                    "Unknown keyword: '" + lexeme + "'",
                    lineNum, col);
        }
        return new Token(type, lexeme, lineNum, col);
    }

    /**
     * Classifies a word into its token type.
     *
     * @param word the word to classify (case-insensitive)
     * @return the TokenType, or null if unrecognized
     */
    private TokenType classifyWord(final String word) {
        switch (word.toUpperCase()) {
            case "DATA":
                return TokenType.DATA;
            case "FILTER":
                return TokenType.FILTER;
            case "MAP":
                return TokenType.MAP;
            case "REDUCE":
                return TokenType.REDUCE;
            case "PRINT":
                return TokenType.PRINT;
            case "SUM":
            case "MAX":
            case "MIN":
            case "AVG":
                return TokenType.REDUCE_OP;
            default:
                return null;
        }
    }

    /**
     * Reads an operator (comparison or arithmetic).
     *
     * <p>
     * Handles two-character operators like &gt;=, &lt;=, == before falling back
     * to single-character ones.</p>
     *
     * @param line the source line
     * @param pos mutable position array
     * @param lineNum the 1-based line number
     * @param col the 1-based column number
     * @return the operator token
     * @throws LexerException if the operator is invalid
     */
    private Token readOperator(final String line,
            final int[] pos,
            final int lineNum,
            final int col) {
        final char c = line.charAt(pos[0]);

        // Try two-character operators first
        if (pos[0] + 1 < line.length()) {
            final String two
                    = line.substring(pos[0], pos[0] + 2);
            if (two.equals(">=")
                    || two.equals("<=")
                    || two.equals("==")) {
                pos[0] += 2;
                return new Token(
                        TokenType.COMPARISON_OP,
                        two, lineNum, col);
            }
        }

        // Single-character operators
        pos[0]++;
        final String lexeme = String.valueOf(c);

        if (c == '>' || c == '<' || c == '=') {
            if (c == '=') {
                // Single '=' is not valid; only '==' is
                throw new LexerException(
                        "Invalid operator '='. "
                        + "Did you mean '=='?",
                        lineNum, col);
            }
            return new Token(
                    TokenType.COMPARISON_OP,
                    lexeme, lineNum, col);
        }
        if (c == '+' || c == '-'
                || c == '*' || c == '/') {
            return new Token(
                    TokenType.ARITHMETIC_OP,
                    lexeme, lineNum, col);
        }

        throw new LexerException(
                "Invalid operator: '" + lexeme + "'",
                lineNum, col);
    }

    /**
     * Checks whether a character can start an operator.
     *
     * @param c the character to check
     * @return true if c is an operator character
     */
    private boolean isOperatorStart(final char c) {
        return c == '>' || c == '<' || c == '='
                || c == '+' || c == '-'
                || c == '*' || c == '/';
    }
}
