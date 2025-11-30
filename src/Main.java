import LexicalAnalyzer.LexicalAnalyzer;
import Parser.Parser;
import Parser.ParseTree;
import llvmGenerator.LLVMGenerator;
import java.util.List;
import java.util.Arrays;
import java.io.*;

/**
 * Entry point for the compiler pipeline: runs the lexical analyzer, parser, and LLVM code generator.
 * This class orchestrates three main steps:
 * - Invokes LexicalAnalyzer to process the source program and writes its output
 * - Creates a Parser, parses the program, and optionally exports the parse tree to LaTeX if the -wt flag is provided
 * - Generates LLVM IR code from the parse tree and writes it to output file
 * Any exceptions during compilation are caught and reported to System.err.
 *
 * @author Alex Bataille and Mohamed Tajani
 */
public class Main {

    /**
     * Main entry point for the compiler pipeline
     *
     * @param args command-line arguments: [-wt latex_file] source_file
     * @throws FileNotFoundException if the lexical analyzer output file cannot be created
     */
    public static void main(String[] args) throws FileNotFoundException {
        PrintStream console = System.out;
        PrintStream lex_out = null;
        String defaultLexFile = "test/LexicalAnalyzerOutput.txt";
        String usedLexFile = defaultLexFile;
        try {
            lex_out = new PrintStream(new java.io.FileOutputStream(new java.io.File(defaultLexFile)));
        } catch (java.io.FileNotFoundException e) {
            // Fallback: use a timestamped alternative file and warn the user
            usedLexFile = "test/LexicalAnalyzerOutput_" + System.currentTimeMillis() + ".txt";
            System.err.println("Warning: could not open '" + defaultLexFile + "' (locked). Using '" + usedLexFile + "' instead.");
            try {
                lex_out = new PrintStream(new java.io.FileOutputStream(new java.io.File(usedLexFile)));
            } catch (java.io.FileNotFoundException ex) {
                System.err.println("Fatal: cannot write lexical output file; exiting.");
                ex.printStackTrace();
                return;
            }
        }

        System.setOut(lex_out);
        try {
            LexicalAnalyzer.main(Arrays.copyOfRange(args, args.length-1, args.length));
        } finally {
            System.setOut(console);
            if (lex_out != null) lex_out.close();
        }
        // Start the parsing
        try {
            // Creates the parser
            Parser parser = new Parser();
            parser.startParsing();
            
            // Optionally export parse tree to LaTeX
            if (args.length == 3 && args[0].equals("-wt")) {
                parser.buildTree(args[1]);
            }
            
            // Generate LLVM IR code
            ParseTree parseTree = parser.getParseTree();
            LLVMGenerator llvmGen = new LLVMGenerator();
            String llvmCode = llvmGen.generate(parseTree);
            
            // Determine output filename based on input filename
            String inputFile = args[args.length - 1];
            String outputFile = inputFile.replace(".ycc", ".ll");
            if (outputFile.equals(inputFile)) {
                outputFile = inputFile + ".ll";
            }
            
            // Write LLVM IR code to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.print(llvmCode);
                System.out.println("LLVM IR code generated successfully: " + outputFile);
            } catch (IOException e) {
                System.err.println("Error writing LLVM output file: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error during compilation:");
            System.err.println(e.toString());
            e.printStackTrace();
        }

    }
}
