import java.util.Vector;

public class SymbolTable {
    private Vector<Symbol> table;

    public SymbolTable() {
        this.table = new Vector<Symbol>();
    }

    public void addSymbol(LexicalUnit lex_unit, int line, int column, String value) {
        if (lex_unit != LexicalUnit.VARNAME) return;
        for (Symbol symbol : this.table) {
            if (symbol.getValue().equals(value)) return;
        }
        Symbol new_symbol = new Symbol(lex_unit, line, column, value);
        this.table.add(new_symbol);
        reorderTable();
    }

    public void printTable() {
        System.out.println("Variables");
        for (Symbol symbol : this.table) {
            System.out.println(symbol.getValue() + " " + String.valueOf(symbol.getLine()));
        }
    }
    
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
