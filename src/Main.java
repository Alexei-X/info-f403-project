import LexicalAnalyzer.LexicalAnalyzer;
import Parser.Parser;
import java.util.List;
import java.io.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream lex_out = new PrintStream(new File("test/LexicalAnalyzerOutput.txt"));
        PrintStream console = System.out;
        System.setOut(lex_out);
        LexicalAnalyzer.main(args);
        System.setOut(console);
        lex_out.close();
        // Lancer le parsing
        try {
            // Créer le parser
            Parser parser = new Parser();
            parser.parseProgram();
        } catch (Exception e) {
            System.err.println("Error parsing program");
        }

    }
}
