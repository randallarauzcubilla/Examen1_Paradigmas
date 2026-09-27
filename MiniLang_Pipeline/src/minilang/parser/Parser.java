package minilang.parser;

import java.util.ArrayList;
import java.util.List;

import minilang.exception.ParserException;
import minilang.lexer.Token;
import minilang.lexer.TokenType;
import minilang.model.DataInstruction;
import minilang.model.FilterInstruction;
import minilang.model.Instruction;
import minilang.model.MapInstruction;
import minilang.model.PrintInstruction;
import minilang.model.ReduceInstruction;

/**
 * Syntactic analyzer for the MiniLang language.
 *
 * <p>
 * This class validates the token stream against the MiniLang grammar and builds
 * the instruction hierarchy. It follows the Single Responsibility Principle: it
 * only validates structure and builds objects.</p>
 *
 * <p>
 * Grammar validated:</p>
 * <pre>
 * program  ::= data operation {operation} "PRINT"
 * data     ::= "DATA" number {number}
 * filter   ::= "FILTER" comparator number
 * map      ::= "MAP" arithmetic number
 * reduce   ::= "REDUCE" ("SUM"|"MAX"|"MIN"|"AVG")
 * </pre>
 *
 * <p>
 * Extension: AVG added to REDUCE, '/' added to MAP arithmetic operators
 * (allowed by exam specifications).</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public final class Parser {

    /**
     * Token lines produced by the Lexer.
     */
    private final List<List<Token>> tokenLines;

    /**
     * Constructs a Parser with pre-tokenized lines.
     *
     * @param tokenLines token lists from the Lexer
     * @throws IllegalArgumentException if tokenLines is null
     */
    public Parser(final List<List<Token>> tokenLines) {
        if (tokenLines == null) {
            throw new IllegalArgumentException(
                    "Token lines cannot be null");
        }
        this.tokenLines = new ArrayList<>(tokenLines);
    }

    /**
     * Parses all token lines into an instruction list.
     *
     * <p>
     * Validates the overall program structure:</p>
     * <ul>
     * <li>First instruction must be DATA</li>
     * <li>Last instruction must be PRINT</li>
     * <li>At least one operation between them</li>
     * </ul>
     *
     * @return the list of parsed instructions
     * @throws ParserException if the grammar is violated
     */
    public List<Instruction> parse() {
        if (tokenLines.isEmpty()) {
            throw new ParserException(
                    "Empty program: expected at least "
                    + "DATA, one operation, and PRINT", 0);
        }

        final List<Instruction> instructions
                = new ArrayList<>();

        for (int i = 0; i < tokenLines.size(); i++) {
            final List<Token> line = tokenLines.get(i);
            final Instruction instr
                    = parseLine(line, i);
            instructions.add(instr);
        }

        validateStructure(instructions);
        return instructions;
    }

    /**
     * Parses a single line of tokens into an Instruction.
     *
     * @param tokens the tokens of one line
     * @param lineIdx the 0-based line index
     * @return the parsed instruction
     * @throws ParserException if the line is invalid
     */
    private Instruction parseLine(final List<Token> tokens,
            final int lineIdx) {
        if (tokens.isEmpty()) {
            throw new ParserException(
                    "Empty instruction line",
                    lineIdx + 1);
        }

        final Token first = tokens.get(0);
        final int lineNum = first.getLine();

        switch (first.getType()) {
            case DATA:
                return parseData(tokens, lineNum);
            case FILTER:
                return parseFilter(tokens, lineNum);
            case MAP:
                return parseMap(tokens, lineNum);
            case REDUCE:
                return parseReduce(tokens, lineNum);
            case PRINT:
                return parsePrint(tokens, lineNum);
            default:
                throw new ParserException(
                        "Expected instruction keyword, "
                        + "found: '"
                        + first.getLexeme() + "'",
                        lineNum);
        }
    }

    /**
     * Parses a DATA instruction.
     *
     * <p>
     * Expected format: DATA number {number}</p>
     *
     * @param tokens the token list
     * @param lineNum the source line number
     * @return a DataInstruction object
     * @throws ParserException if format is invalid
     */
    private DataInstruction parseData(
            final List<Token> tokens,
            final int lineNum) {

        if (tokens.size() < 2) {
            throw new ParserException(
                    "DATA requires at least one number",
                    lineNum);
        }

        final List<Integer> values = new ArrayList<>();
        for (int i = 1; i < tokens.size(); i++) {
            final Token t = tokens.get(i);
            if (t.getType() != TokenType.INTEGER) {
                throw new ParserException(
                        "DATA expects integers, found: '"
                        + t.getLexeme() + "'",
                        lineNum);
            }
            values.add(
                    parseInteger(t.getLexeme(), lineNum));
        }

        try {
            return new DataInstruction(lineNum, values);
        } catch (IllegalArgumentException e) {
            throw new ParserException(
                    e.getMessage(), lineNum);
        }
    }

    /**
     * Parses a FILTER instruction.
     *
     * <p>
     * Expected format: FILTER comparator number</p>
     *
     * @param tokens the token list
     * @param lineNum the source line number
     * @return a FilterInstruction object
     * @throws ParserException if format is invalid
     */
    private FilterInstruction parseFilter(
            final List<Token> tokens,
            final int lineNum) {

        if (tokens.size() != 3) {
            throw new ParserException(
                    "FILTER requires exactly: "
                    + "FILTER operator number",
                    lineNum);
        }

        final Token opToken = tokens.get(1);
        if (opToken.getType()
                != TokenType.COMPARISON_OP) {
            throw new ParserException(
                    "FILTER expects a comparison "
                    + "operator (>, <, >=, <=, ==), "
                    + "found: '"
                    + opToken.getLexeme() + "'",
                    lineNum);
        }

        final Token numToken = tokens.get(2);
        if (numToken.getType() != TokenType.INTEGER) {
            throw new ParserException(
                    "FILTER expects an integer after "
                    + "operator, found: '"
                    + numToken.getLexeme() + "'",
                    lineNum);
        }

        final int threshold
                = parseInteger(numToken.getLexeme(), lineNum);

        try {
            return new FilterInstruction(
                    lineNum, opToken.getLexeme(), threshold);
        } catch (IllegalArgumentException e) {
            throw new ParserException(
                    e.getMessage(), lineNum);
        }

    }

    /**
     * Parses a MAP instruction.
     *
     * <p>
     * Expected format: MAP arithmetic number</p>
     *
     * @param tokens the token list
     * @param lineNum the source line number
     * @return a MapInstruction object
     * @throws ParserException if format is invalid
     */
    private MapInstruction parseMap(
            final List<Token> tokens,
            final int lineNum) {

        if (tokens.size() != 3) {
            throw new ParserException(
                    "MAP requires exactly: "
                    + "MAP operator number",
                    lineNum);
        }

        final Token opToken = tokens.get(1);
        if (opToken.getType()
                != TokenType.ARITHMETIC_OP) {
            throw new ParserException(
                    "MAP expects an arithmetic "
                    + "operator (+, -, *, /), found: '"
                    + opToken.getLexeme() + "'",
                    lineNum);
        }

        final Token numToken = tokens.get(2);
        if (numToken.getType() != TokenType.INTEGER) {
            throw new ParserException(
                    "MAP expects an integer after "
                    + "operator, found: '"
                    + numToken.getLexeme() + "'",
                    lineNum);
        }

        final int operand
                = parseInteger(numToken.getLexeme(), lineNum);
        try {
            return new MapInstruction(
                    lineNum, opToken.getLexeme(), operand);
        } catch (IllegalArgumentException e) {
            throw new ParserException(
                    e.getMessage(), lineNum);
        }
    }

    /**
     * Parses a REDUCE instruction.
     *
     * <p>
     * Expected format: REDUCE (SUM|MAX|MIN|AVG)</p>
     *
     * @param tokens the token list
     * @param lineNum the source line number
     * @return a ReduceInstruction object
     * @throws ParserException if format is invalid
     */
    private ReduceInstruction parseReduce(
            final List<Token> tokens,
            final int lineNum) {

        if (tokens.size() != 2) {
            throw new ParserException(
                    "REDUCE requires exactly one "
                    + "operator (SUM, MAX, MIN, AVG)",
                    lineNum);
        }

        final Token opToken = tokens.get(1);
        if (opToken.getType() != TokenType.REDUCE_OP) {
            throw new ParserException(
                    "REDUCE expects SUM, MAX, MIN or "
                    + "AVG, found: '"
                    + opToken.getLexeme() + "'",
                    lineNum);
        }

        try {
            return new ReduceInstruction(
                    lineNum, opToken.getLexeme());
        } catch (IllegalArgumentException e) {
            throw new ParserException(
                    e.getMessage(), lineNum);
        }
    }

    /**
     * Parses a PRINT instruction.
     *
     * <p>
     * Expected format: PRINT (no arguments)</p>
     *
     * @param tokens the token list
     * @param lineNum the source line number
     * @return a PrintInstruction object
     * @throws ParserException if PRINT has arguments
     */
    private PrintInstruction parsePrint(
            final List<Token> tokens,
            final int lineNum) {

        if (tokens.size() != 1) {
            throw new ParserException(
                    "PRINT does not accept arguments",
                    lineNum);
        }
        return new PrintInstruction(lineNum);
    }

    /**
     * Validates the overall program structure.
     *
     * <p>
     * Rules enforced:</p>
     * <ul>
     * <li>First instruction must be DATA</li>
     * <li>Last instruction must be PRINT</li>
     * <li>At least one operation between them</li>
     * <li>Only one DATA allowed</li>
     * <li>Only one PRINT allowed</li>
     * </ul>
     *
     * @param instructions the parsed instruction list
     * @throws ParserException if structure is invalid
     */
    private void validateStructure(
            final List<Instruction> instructions) {

        final int size = instructions.size();

        // Minimum: DATA + 1 operation + PRINT = 3
        if (size < 3) {
            throw new ParserException(
                    "Program must have at least DATA, "
                    + "one operation, and PRINT",
                    instructions.get(0).getLine());
        }

        // First must be DATA
        if (!(instructions.get(0) instanceof DataInstruction)) {
            throw new ParserException(
                    "Program must start with DATA",
                    instructions.get(0).getLine());
        }

        // Last must be PRINT
        if (!(instructions.get(size - 1) instanceof PrintInstruction)) {
            throw new ParserException(
                    "Program must end with PRINT",
                    instructions.get(size - 1).getLine());
        }

        // Check middle instructions and uniqueness
        int dataCount = 1;
        int printCount = 1;

        for (int i = 1; i < size - 1; i++) {
            final Instruction instr = instructions.get(i);
            if (instr instanceof DataInstruction) {
                dataCount++;
            }
            if (instr instanceof PrintInstruction) {
                printCount++;
            }
        }

        if (dataCount > 1) {
            throw new ParserException(
                    "Only one DATA instruction allowed",
                    instructions.get(0).getLine());
        }
        if (printCount > 1) {
            throw new ParserException(
                    "Only one PRINT instruction allowed",
                    instructions.get(size - 1).getLine());
        }
    }

    /**
     * Safely parses a string into a non-negative integer.
     *
     * @param value the string to parse
     * @param lineNum the source line for error reporting
     * @return the parsed integer value
     * @throws ParserException if parsing fails or is negative
     */
    private int parseInteger(final String value,
            final int lineNum) {
        try {
            final int result = Integer.parseInt(value);
            if (result < 0) {
                throw new ParserException(
                        "Negative numbers not allowed: "
                        + value, lineNum);
            }
            return result;
        } catch (NumberFormatException e) {
            throw new ParserException(
                    "Invalid integer: '" + value + "'",
                    lineNum);
        }
    }
}
