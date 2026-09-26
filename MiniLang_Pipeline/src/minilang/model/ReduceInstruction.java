package minilang.model;

/**
 * Represents a {@code REDUCE} instruction that aggregates a list of integers
 * into a single scalar value.
 *
 * <p>
 * Supported operators: {@code SUM}, {@code AVG}, {@code MAX}, {@code MIN}.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class ReduceInstruction extends Instruction {

    /**
     * Reduction operator name (e.g. "SUM", "AVG").
     */
    private final String operator;

    /**
     * Constructs a ReduceInstruction.
     *
     * @param line source line number (&gt;= 1)
     * @param operator reduction operator (must be valid)
     * @throws IllegalArgumentException if operator is invalid
     */
    public ReduceInstruction(final int line,
            final String operator) {
        super(line);
        this.operator = validateOperator(operator);
    }

    /**
     * Validates that the operator is allowed.
     *
     * @param op the operator to validate
     * @return the normalized (uppercase) operator
     * @throws IllegalArgumentException if invalid
     */
    private String validateOperator(final String op) {
        if (op == null || op.isEmpty()) {
            throw new IllegalArgumentException(
                    "REDUCE operator cannot be null or empty");
        }
        final String upper = op.toUpperCase();
        switch (upper) {
            case "SUM":
            case "AVG":
            case "MAX":
            case "MIN":
                return upper;
            default:
                throw new IllegalArgumentException(
                        "Invalid REDUCE operator: '" + op + "'. "
                        + "Allowed: SUM AVG MAX MIN");
        }
    }

    /**
     * Returns the reduction operator (always uppercase).
     *
     * @return the operator name
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the keyword name.
     *
     * @return the string "REDUCE"
     */
    @Override
    public String getKeyword() {
        return "REDUCE";
    }

    /**
     * IR format: {@code REDUCE: operator}
     *
     * @return the IR line
     */
    @Override
    public String toIR() {
        return "REDUCE: " + operator;
    }
}
