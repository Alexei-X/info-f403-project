import java.util.Vector;

/**
 * Symbol table, storing variables seen by the scanner and the first line where they appeared. 
 *
 */
public class SymbolTable {
    /** vector containing the symbols*/
    private Vector<Symbol> table;

    /**
     * SymboLTable constructor, initializing the vector
     */
    public SymbolTable() {
        this.table = new Vector<Symbol>();
    }

    /**
     * method to add a symbol to the table, adding it if not already met by the scanner, storing the line and column
     * of where it appeared
     * @param lex_unit the lexical unit corresponding to the value (only treating VARNAME)
     * @param line the line at which the lexical unit is
     * @param column the column at which the lexical unit is
     * @param value the value of the lexical unit
     */
    public void addSymbol(LexicalUnit lex_unit, int line, int column, String value) {
        if (lex_unit != LexicalUnit.VARNAME) return;
        for (Symbol symbol : this.table) {
            if (symbol.getValue().equals(value)) return;
        }
        Symbol new_symbol = new Symbol(lex_unit, line, column, value);
        this.table.add(new_symbol);
        reorderTable();
    }

    /**
     * prints the table to standard output
     */
    public void printTable() {
        System.out.println("Variables");
        for (Symbol symbol : this.table) {
            System.out.println(symbol.getValue() + " " + String.valueOf(symbol.getLine()));
        }
    }
    
    /**
     * reorder the table in alphabetical order of the variables names
     */
    private void reorderTable() {
        int n = table.size();
        for (int i = 0; i < n; i++) {
            int min = i;
            for (int j = i+1; j < n; j++) {
                String j_value = (String) table.get(j).getValue();
                if (j_value.compareTo((String) (table.get(min).getValue())) < 0) min = j;
            }
            Symbol temp = table.get(i);
            table.set(i, table.get(min));
            table.set(min, temp);
        }
    }

}
