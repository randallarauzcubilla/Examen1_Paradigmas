package minilang.model;

/**
 * Abstract base class for all MiniLang instructions.
 *
 * <p>
 * Applies the Open-Closed Principle (OCP): the type is closed for modification
 * but open for extension via new subclasses.</p>
 * <p>
 * Applies the Liskov Substitution Principle (LSP): every subclass must honor
 * the contract defined here.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public abstract class Instruction {

    /**
     * Source line where this instruction was declared (1-based).
     */
    private final int line;

    /**
     * Protected constructor for subclasses.
     *
     * @param line source line number (must be &gt;= 1)
     * @throws IllegalArgumentException if line is invalid
     */
    protected Instruction(final int line) {
        if (line < 1) {
            throw new IllegalArgumentException(
                    "Instruction line must be >= 1, got: " + line);
        }
        this.line = line;
    }

    /**
     * Returns the source line number where the instruction was declared.
     *
     * @return the line number (1-based)
     */
    public int getLine() {
        return line;
    }

    /**
     * Converts this instruction into its Intermediate Representation (IR)
     * format.
     *
     * <p>
     * The IR line must be a single line with no trailing newline
     * characters.</p>
     *
     * @return the IR string representation (never null)
     */
    public abstract String toIR();

    /**
     * Returns the keyword name of this instruction as it appears in the
     * MiniLang grammar.
     *
     * @return the keyword name (never null, never empty)
     */
    public abstract String getKeyword();
}
