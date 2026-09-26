package minilang.model;

/**
 * Represents a {@code PRINT} instruction that outputs the final result of the
 * pipeline. Always the last instruction.
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class PrintInstruction extends Instruction {

    /**
     * Constructs a PrintInstruction.
     *
     * @param line source line number (&gt;= 1)
     */
    public PrintInstruction(final int line) {
        super(line);
    }

    /**
     * Returns the keyword name.
     *
     * @return the string "PRINT"
     */
    @Override
    public String getKeyword() {
        return "PRINT";
    }

    /**
     * IR format: {@code PRINT: }
     *
     * @return the IR line (keyword with empty payload)
     */
    @Override
    public String toIR() {
        return "PRINT: ";
    }
}
