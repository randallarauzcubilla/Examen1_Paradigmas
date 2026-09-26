package minilang.ir;

import java.util.List;

import minilang.model.Instruction;

/**
 * Generates the Intermediate Representation (IR) file content from a list of
 * parsed instructions.
 *
 * <p>
 * The IR format is the data contract between the Java stage and the Python
 * stage. Each instruction produces exactly one line in the IR output.</p>
 *
 * <p>
 * IR line format examples:</p>
 * <pre>
 * DATA: 3,8,5,10,12
 * FILTER: >,5
 * MAP: *,2
 * REDUCE: SUM
 * PRINT:
 * </pre>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class IRGenerator {

    /**
     * Line separator for the IR output.
     */
    private static final String NEWLINE
            = System.lineSeparator();

    /**
     * Private constructor to prevent instantiation. This class provides only
     * static utility methods.
     *
     * @throws UnsupportedOperationException always
     */
    private IRGenerator() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated");
    }

    /**
     * Generates the complete IR content from instructions.
     *
     * <p>
     * Uses polymorphism: each instruction's toIR() method is called without
     * knowing its concrete type.</p>
     *
     * @param instructions the parsed instruction list
     * @return the IR content as a single string
     * @throws IllegalArgumentException if list is null/empty
     */
    public static String generate(
            final List<Instruction> instructions) {

        if (instructions == null
                || instructions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Instruction list cannot be "
                    + "null or empty");
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) {
                sb.append(NEWLINE);
            }
            // Polymorphism in action:
            // each subclass provides its own toIR()
            sb.append(instructions.get(i).toIR());
        }
        return sb.toString();
    }
}
