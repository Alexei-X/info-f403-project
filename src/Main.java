import LexicalAnalyzer.LexicalAnalyzer;
import Parser.Parser;
import java.util.List;
import java.util.Arrays;
import java.io.*;

/**
 * Entry point for the compiler pipeline: runs the lexical analyzer and then the parser.
 * This class orchestrates two main steps:
 * - Invokes LexicalAnalyzer to process the source program and writes its output
 * - Creates a Parser, parses the program, and optionally exports the parse tree
 *   to LaTeX if the -wt flag is provided.
 * Any exceptions during parsing are caught and reported to System.err.
 *
 * @author Alex Bataille and Mohamed Tajani
 */
public class Main {

    /**
     * Main entry point for the compiler pipeline
     *
     * @param args ommand-line arguments
     * @throws FileNotFoundException if the lexical analyzer output file cannot be created
     */
    public static void main(String[] args) throws FileNotFoundException {
        PrintStream lex_out = new PrintStream(new File("test/LexicalAnalyzerOutput.txt"));
        PrintStream console = System.out;
        System.setOut(lex_out);
        LexicalAnalyzer.main(Arrays.copyOfRange(args, args.length-1, args.length));
        System.setOut(console);
        lex_out.close();
        // Stars the parsing
        try {
            // Creates the parser
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
