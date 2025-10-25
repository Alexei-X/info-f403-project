import java.io.*;
import java.util.List;

/**
 * Main class of the program
 */
public class Main {
    /** Launch the lexical analyzer and parser with input file 
     * @param args arguments of the program (should contain file path)
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <inputfile>");
            return;
        }
        try {
            LexicalAnalyzer scanner = new LexicalAnalyzer(new FileReader(args[0]));
            while (!scanner.yyatEOF()) {
                scanner.yylex();
            }
            List<Symbol> tokens = scanner.getTokens();
            // Now that we have the tokens,
            // Initialize parser with them
            Parser parser = new Parser(tokens);
            Parser.ProgramNode ast = parser.parseProgram();
            System.out.println("Parsing successful!");
            System.out.println("AST:");
            System.out.println(ast);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + args[0]);
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        } catch (Parser.ParseException e) {
            System.out.println("Parse error: " + e.getMessage());
        }
    }
}

