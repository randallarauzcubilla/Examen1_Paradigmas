package minilang.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a {@code DATA} instruction that introduces an initial list of
 * integer values into the pipeline.
 *
 * <p>
 * Immutable: the internal list cannot be modified after construction, enforcing
 * defensive-copy safety.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class DataInstruction extends Instruction {

    /**
     * The list of integer values (defensively copied).
     */
    private final List<Integer> values;

    /**
     * Constructs a DataInstruction with a list of values.
     *
     * @param line source line number (&gt;= 1)
     * @param values the integer values (must not be null or empty)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public DataInstruction(final int line,
            final List<Integer> values) {
        super(line);
        validateValues(values);
        this.values = Collections.unmodifiableList(
                new ArrayList<>(values));
    }

    /**
     * Validates the input value list.
     *
     * @param values the list to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateValues(final List<Integer> values) {
        if (values == null) {
            throw new IllegalArgumentException(
                    "DATA values list cannot be null");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException(
                    "DATA values list cannot be empty");
        }
        for (Integer v : values) {
            if (v == null) {
                throw new IllegalArgumentException(
                        "DATA values cannot contain null");
            }
        }
    }

    /**
     * Returns a defensive copy of the values list.
     *
     * @return an unmodifiable list of integers
     */
    public List<Integer> getValues() {
        return values;
    }

    /**
     * Returns the keyword name.
     *
     * @return the string "DATA"
     */
    @Override
    public String getKeyword() {
        return "DATA";
    }

    /**
     * IR format: {@code DATA: n1,n2,n3,...}
     *
     * @return the IR line
     */
    @Override
    public String toIR() {
        final StringBuilder sb = new StringBuilder("DATA: ");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
