package LexicalAnalyzer;

import java.util.ArrayList;

/**
 * Sequence of tokens containing a list of symbols
 * @author Alex Bataille
*/
public class TokenSequence {
    /**
     * ArrayList containing the symbol met by the scanner at a given time
    */
    private ArrayList<Symbol> sequence;
    
    /**
     * Constructor of TokenSequence, initalizing the List
    */
    public TokenSequence() {
        this.sequence = new ArrayList<Symbol>();
    }

    /**
     * Add a symbol to the sequence if not already containing
     * @param lexical_unit the corresponding lexical unit (from LexicalUnit enum)
     * @param value the value of the token
     */
    public void addSymbol(LexicalUnit lexical_unit, String value) {
        Symbol token = new Symbol(lexical_unit, value);
        if (!this.sequence.contains(token)) {
            this.sequence.add(token);
        }
    }

    /**
     * Overloaded: add a symbol with a source line number
     *
     * @param lexical_unit the lexical unit
     * @param value the value of lexical unit
     * @param line the line of the lexical unit
     */
    public void addSymbol(LexicalUnit lexical_unit, String value, int line) {
        Symbol token = new Symbol(lexical_unit, line, -1, value);
        if (!this.sequence.contains(token)) {
            this.sequence.add(token);
        }
    }

    /**
     * Prints the symbol sequence to the output
     */
    public void printSequence() {
        for (Symbol symbol : sequence) {
            int line = symbol.getLine();
            String prefix = (line > 0) ? ("line: " + line + " ") : "";
            System.out.println(prefix + symbol.toString());
        }
    }
}
