import java.util.ArrayList;

public class SymbolTable {
    private ArrayList<Symbol> table;
    
    public SymbolTable() {
        this.table = new ArrayList<Symbol>();
    }

    public void addSymbol(LexicalUnit lexical_unit, String value) {
        Symbol token = new Symbol(lexical_unit, value);
        if (this.table.contains(token)) {
            this.table.add(token);
        }
    }

    public void printTable() {
        for (Symbol symbol : table) {
            System.out.println(symbol.toString());
        }
    }
}

