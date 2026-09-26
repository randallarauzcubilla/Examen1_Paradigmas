package minilang.model;

/**
 * Represents a {@code MAP} instruction that applies an arithmetic operation
 * with a fixed operand to each element of the list.
 *
 * <p>
 * Supported operators: {@code +}, {@code -}, {@code *}, {@code /}.</p>
 * <p>
 * Defensive design: division by zero is rejected at construction time, not at
 * execution time.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class MapInstruction extends Instruction {

    /**
     * Arithmetic operator symbol (+, -, *, /).
     */
    private final String operator;

    /**
     * Operand to apply to each list element.
     */
    private final int operand;

    /**
     * Constructs a MapInstruction.
     *
     * @param line source line number (&gt;= 1)
     * @param operator arithmetic operator (must be valid)
     * @param operand the operand value
     * @throws IllegalArgumentException if invalid or div by zero
     */
    public MapInstruction(final int line,
            final String operator,
            final int operand) {
        super(line);
        this.operator = validateOperator(operator);
        this.operand = operand;
        preventDivisionByZero();
    }

    /**
     * Validates that the operator is allowed.
     *
     * @param op the operator to validate
     * @return the validated operator
     * @throws IllegalArgumentException if invalid
     */
    private String validateOperator(final String op) {
        if (op == null || op.length() != 1) {
            throw new IllegalArgumentException(
                    "MAP operator must be a single character");
        }
        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
                return op;
            default:
                throw new IllegalArgumentException(
                        "Invalid MAP operator: '" + op + "'. "
                        + "Allowed: + - * /");
        }
    }

    /**
     * Prevents division by zero at construction time.
     *
     * @throws IllegalArgumentException if dividing by zero
     */
    private void preventDivisionByZero() {
        if ("/".equals(operator) && operand == 0) {
            throw new IllegalArgumentException(
                    "MAP cannot divide by zero");
        }
    }

    /**
     * Returns the arithmetic operator.
     *
     * @return the operator symbol
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the operand value.
     *
     * @return the operand integer
     */
    public int getOperand() {
        return operand;
    }

    /**
     * Returns the keyword name.
     *
     * @return the string "MAP"
     */
    @Override
    public String getKeyword() {
        return "MAP";
    }

    /**
     * IR format: {@code MAP: operator,value}
     *
     * @return the IR line
     */
    @Override
    public String toIR() {
        return String.format(
                "MAP: %s,%d", operator, operand);
    }
}
