import java.util.ArrayList;

/**
 * Symbol table object containing a list of symbols
 * @author Alex Bataille
*/
public class SymbolTable {
    /**
     * ArrayList containing the symbol met by the scanner at a given time
    */
    private ArrayList<Symbol> table;
    
    /**
     * Constructor of SymbolTable, initalizing the List
    */
    public SymbolTable() {
        this.table = new ArrayList<Symbol>();
    }

    /**
     * Add a symbol to the table if not already containing
     * @param lexical_unit the corresponding lexical unit (from LexicalUnit enum)
     * @param value the value of the token
     */
    public void addSymbol(LexicalUnit lexical_unit, String value) {
        Symbol token = new Symbol(lexical_unit, value);
        if (!this.table.contains(token)) {
            this.table.add(token);
        }
    }

    /**
     * Prints the symbol table to the output
     */
    public void printTable() {
        for (Symbol symbol : table) {
            System.out.println(symbol.toString());
        }
    }
}

