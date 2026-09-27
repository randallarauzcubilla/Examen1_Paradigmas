package minilang.model;

/**
 * Represents a {@code FILTER} instruction that keeps only elements satisfying a
 * comparison against a threshold value.
 *
 * <p>
 * Supported operators: {@code >}, {@code <}, {@code >=},
 * {@code <=}, {@code ==}.</p>
 *
 * <p>
 * Contract: Only operators that the Lexer can produce are accepted. The '='
 * operator is NOT supported (use '==' instead).</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class FilterInstruction extends Instruction {

    /**
     * Comparison operator symbol.
     */
    private final String operator;

    /**
     * Threshold value for the comparison.
     */
    private final int threshold;

    /**
     * Constructs a FilterInstruction.
     *
     * @param line source line number (&gt;= 1)
     * @param operator comparison operator (must be valid)
     * @param threshold the comparison value
     * @throws IllegalArgumentException if operator is invalid
     */
    public FilterInstruction(final int line,
            final String operator,
            final int threshold) {
        super(line);
        this.operator = validateOperator(operator);
        this.threshold = threshold;
    }

    /**
     * Validates that the operator is allowed.
     *
     * <p>
     * Only accepts operators that the Lexer can produce:
     * {@code >}, {@code <}, {@code >=}, {@code <=}, {@code ==}.</p>
     *
     * @param op the operator to validate
     * @return the validated operator
     * @throws IllegalArgumentException if invalid
     */
    private String validateOperator(final String op) {
        if (op == null || op.isEmpty()) {
            throw new IllegalArgumentException(
                    "FILTER operator cannot be null or empty");
        }
        switch (op) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
                return op;
            default:
                throw new IllegalArgumentException(
                        "Invalid FILTER operator: '" + op
                        + "'. Allowed: > < >= <= ==");
        }
    }

    /**
     * Returns the comparison operator.
     *
     * @return the operator symbol
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the threshold value.
     *
     * @return the threshold integer
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Returns the keyword name.
     *
     * @return the string "FILTER"
     */
    @Override
    public String getKeyword() {
        return "FILTER";
    }

    /**
     * IR format: {@code FILTER: operator,value}
     *
     * @return the IR line
     */
    @Override
    public String toIR() {
        return String.format(
                "FILTER: %s,%d", operator, threshold);
    }
}
