import LexicalAnalyzer.LexicalAnalyzer;
import Parser.Parser;
import java.util.List;
import java.util.Arrays;
import java.io.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        PrintStream lex_out = new PrintStream(new File("test/LexicalAnalyzerOutput.txt"));
        PrintStream console = System.out;
        System.setOut(lex_out);
        LexicalAnalyzer.main(Arrays.copyOfRange(args, args.length-1, args.length));
        System.setOut(console);
        lex_out.close();
        // Lancer le parsing
        try {
            // Créer le parser
            Parser parser = new Parser();
            parser.startParsing();
            if (args.length == 3 && args[0].equals("-wt")) {
                parser.buildTree(args[1]);
            }
        } catch (Exception e) {
            System.err.println("Error parsing program");
            System.err.println(e.toString());
        }

    }
}
