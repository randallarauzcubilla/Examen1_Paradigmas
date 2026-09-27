package minilang.lexer;

/**
 * Enumeration of all token types recognized by the MiniLang lexer.
 *
 * <p>
 * Follows the Single Responsibility Principle: each token has one clear role in
 * the grammar.</p>
 *
 * @author Randall AC
 * @author Keilor MC
 * @version 1.0
 */
public enum TokenType {

    /**
     * {@code DATA} keyword: introduces a list of integers.
     */
    DATA,
    
    /**
     * {@code FILTER} keyword: filters the current list.
     */
    FILTER,
    
    /**
     * {@code MAP} keyword: transforms each element.
     */
    MAP,
    
    /**
     * {@code REDUCE} keyword: aggregates the list to a scalar.
     */
    REDUCE,
    
    /**
     * {@code PRINT} keyword: outputs the final result.
     */
    PRINT,
    
    /**
     * Integer literal (e.g. {@code 42}).
     */
    INTEGER,
    
    /**
     * Comparison operator. 
     * Allowed: {@code >}, {@code <}, {@code >=}, {@code <=}, {@code ==}.
     */
    COMPARISON_OP,
    
    /**
     * Arithmetic operator. 
     * Allowed: {@code +}, {@code -}, {@code *}, {@code /}.
     */
    ARITHMETIC_OP,
    
    /**
     * Reduce operator. 
     * Allowed: {@code SUM}, {@code AVG}, {@code MAX}, {@code MIN}.
     */
    REDUCE_OP
    // Note: EOF and UNKNOWN were removed as the Lexer throws exceptions 
    // or stops naturally, never emitting these synthetic tokens.
}