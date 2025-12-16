package Parser;

import java.util.List;
import java.util.Arrays;
import java.io.*;

import LexicalAnalyzer.LexicalUnit;
import LexicalAnalyzer.Symbol;
import LexicalAnalyzer.NonTermUnit;

/**
 * Recursive descent parser corresponding to yalcc language
 * The parser reads the output of the scanner in test/output/LexicalAnalyzerOutput.txt, and finds 
 * the corresponding left most derivation, outputing the rule number on stdout
 * It also builds a ParseTree, representing the program according to the grammar.  
 *
 * @author Mohamed Tajani and Alex Bataille
 */
public class Parser{
    /** List of LexicalUnit storing the lexical units of the program*/
    private List<LexicalUnit> lexunits;
    /** List of tokens storing the corresponding values of the program*/
    private List<String> tokens;
    /** current index at which we are looking at in lexunits and tokens*/
    private int currentLexicalUnitIndex;
    /** List of the rawlines of the program*/
    private static java.util.List<String> tokenRawLines = new java.util.ArrayList<>();
    /** List of the raw line numbers of the program*/
    private static java.util.List<Integer> tokenLineNumbers = new java.util.ArrayList<>();
    /** ParseTree of the program, built by recursion with children*/
    private ParseTree tree;

    /**
     * Creates a new Parser and loads LexicalUnits and tokens from the scanner output file
     *
     * @throws Exception if the lexical units file cannot be read or parsed
     */
    public Parser() throws Exception {
        Pair<List<LexicalUnit>, List<String>> res = readLexicalUnitsFromFile("test/output/LexicalAnalyzerOutput.txt");
        this.lexunits = res.getFirst();
        this.tokens = res.getSecond();
        this.currentLexicalUnitIndex = 0;
    }

    /**
     * Throws a ParseException with location in the ycc file, otherwise the token index
     *
     * @param message human-readable error description
     * @throws ParseException
     */
    private void throwParseError(String message) throws ParseException {
        int idx = currentLexicalUnitIndex;
        String raw = "n/a";
        int srcLine = -1;
        if (tokenRawLines != null && idx >= 0 && idx < tokenRawLines.size()) raw = tokenRawLines.get(idx);
        if (tokenLineNumbers != null && idx >= 0 && idx < tokenLineNumbers.size()) srcLine = tokenLineNumbers.get(idx);
        String loc = srcLine > 0 ? "line " + srcLine : "token index " + idx;
        throw new ParseException(message + " at " + loc + " -> " + raw);
    }

    /**
     * Returns the current lexical unit without consuming it
     *
     * @return the current lexical unit or EOS if end of stream
     */
    private LexicalUnit lookCurrent(){
        if (currentLexicalUnitIndex < lexunits.size()){
            return lexunits.get(currentLexicalUnitIndex);
        }
        return LexicalUnit.EOS;
    }

    /**
     * Consumes the current lexical unit if it matches the expected one
     *
     * @param expected the expectex lexical unit
     * @return a ParseTree node wrapping the matched symbol
     * @throws ParseException if current different than expected
     */
    private ParseTree match(LexicalUnit expected) throws ParseException {
        if (lookCurrent() == expected) {
            String value = tokens.get(currentLexicalUnitIndex);
            currentLexicalUnitIndex++;
            return new ParseTree(new Symbol(expected, value));
        } else{
            throwParseError("Error: Expected " + expected + " got " + lookCurrent());
            return new ParseTree(new Symbol(LexicalUnit.EOS, ""));
        } 
    }

    /**
     * Parses the input starting from the grammar entry {@code <Program>}
     *
     * @throws ParseException if error occurs during parsing
     */
    public void startParsing() throws ParseException {
        tree = parseProgram();
    }

    /** 
     * Production 1 : 
     * {@code <Program>} -> Prog [ProgName] Is {@code <Code>} End
     *
     * @return The ParseTree node for {@code <Program>}
     * @throws ParseException if any component fails to parse or trailing tokens remain
     */
    private ParseTree parseProgram() throws ParseException {
        System.out.println("1 ");
        List<ParseTree> childrens = new java.util.ArrayList<>();
        
        childrens.add(match(LexicalUnit.PROG));
        childrens.add(match(LexicalUnit.PROGNAME));
        childrens.add(match(LexicalUnit.IS));
        childrens.add(parseCode());
        childrens.add(match(LexicalUnit.END));

        if (lookCurrent() != LexicalUnit.EOS){
            throwParseError("Unexpected token after end of program");
        }

        return new ParseTree(new Symbol(NonTermUnit.Program, "\\textless Prog\\textgreater"), childrens);
    }


    /**
     * Productions 2 and 3 :
     * Parses the non terminal {@code <Code>} which can be :
     * - {@code <Instruction>} ; {@code <Code>}
     * - epsilon
     *
     * @return The ParseTree node for {@code <Code>}
     * @throws ParseException if any component fails to parse
     */
    public ParseTree parseCode() throws ParseException {
        System.out.println("2 ");
        LexicalUnit nextLexicalUnit = lookCurrent();
        //  Rule 2 :
        // First¹((Instruction); (Code) Follow¹(Code))) = {[VarName], If, While, Print, Input} [cite: 8]
        if (nextLexicalUnit == LexicalUnit.VARNAME || nextLexicalUnit == LexicalUnit.IF ||
            nextLexicalUnit == LexicalUnit.WHILE || nextLexicalUnit == LexicalUnit.PRINT || nextLexicalUnit == LexicalUnit.INPUT){
                List<ParseTree> childrens = new java.util.ArrayList<>(); 
                childrens.add(parseInstruction());
                childrens.add(match(LexicalUnit.SEMI));
                childrens.add(parseCode());
                return new ParseTree(new Symbol(NonTermUnit.Code, "\\textless Code\\textgreater"), childrens);
        }

        // Rule 3 : <Code> -> epsilon
        // Follow¹(<Code>) = {End, Else} [cite: 10]
        else if (nextLexicalUnit == LexicalUnit.END || nextLexicalUnit == LexicalUnit.ELSE){
            System.out.println("3 ");
            return new ParseTree(new Symbol(NonTermUnit.Code, "\\textless Code\\textgreater"));
        }
        
        else {
            throwParseError("Error : Unexpected token in <Code> : " + nextLexicalUnit);
            return new ParseTree(new Symbol(LexicalUnit.EOS, ""));
        }
    }

    /**
     * Parses the non terminal {@code <Instruction>}
     *
     * @return The ParseTree node for {@code <Instruction>}
     * @throws ParseException if any component fails to parse
     */
    public ParseTree parseInstruction() throws ParseException{
        
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.Instruction, "\\textless Instruction\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        
        if (nextLexicalUnit == LexicalUnit.VARNAME) {
            // Rule 4 : [VarName] (Implies <Assign>)
            System.out.println("4 ");
            childrens.add(parseAssign());

        }
        else if (nextLexicalUnit == LexicalUnit.IF){
            // Rule 5 : If [cite: 12]
            System.out.println("5 ");
            childrens.add(parseIf());
        }
        else if (nextLexicalUnit == LexicalUnit.WHILE){
            // Rule 6 : While [cite: 13]
            System.out.println("6 ");
            childrens.add(parseWhile());
        }
        else if (nextLexicalUnit == LexicalUnit.PRINT){
            // Rule 7 : Print (Implies <Output>) [cite: 14]
            System.out.println("7 ");
            childrens.add(parseOutput());
        }
        else if (nextLexicalUnit == LexicalUnit.INPUT){
            // Rule 8 : Input (Implies <Input>) [cite: 15]
            System.out.println("8 ");
            childrens.add(parseInput());
        }
        return new ParseTree(retSymb, childrens);
    }

    /**
     * Parses the non terminal {@code <Assign>}
     * Rule 10 : {@code <Assign>} -> [VarName] = {@code <ExprArith>}
     *
     * @return The ParseTree node for {@code <Assign>}
     * @throws ParseException on mismatch or expression errors
     */
    public ParseTree parseAssign() throws ParseException{    
        System.out.println("10 ");

        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        childrens.add(match(LexicalUnit.VARNAME));
        childrens.add(match(LexicalUnit.ASSIGN));
        childrens.add(parseExprArith());
        
        return new ParseTree(new Symbol(NonTermUnit.Assign, "\\textless Assign\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <ExprArith>}
     * Rule 11 : ⟨Prod⟩⟨ExprArith′⟩ · Follow1(⟨ExprArith⟩)= [VarName], [Number], −,(
     *
     * @return The ParseTree node for {@code <ExprArith>}
     * @throws ParseException if sub-steps of parsing fail
     */
    public ParseTree parseExprArith() throws ParseException{
        System.out.println("10 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(parseProd());
        childrens.add(parseExprArithPrime());

        return new ParseTree(new Symbol(NonTermUnit.ExprArith, "\\textless ExprArith\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <ExprArith'>}
     *
     * @return The ParseTree node for {@code <ExprArith'>}
     * @throws ParseException if unexpected token or error in sub-steps
     */
    public ParseTree parseExprArithPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.ExprArithp, "\\textless ExprArith\\textquotesingle\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        if (nextLexicalUnit == LexicalUnit.PLUS) {
            // Rule 12 :
            System.out.println("12 ");
            childrens.add(match(LexicalUnit.PLUS));
            childrens.add(parseProd());
            childrens.add(parseExprArithPrime());
        }
        else if (nextLexicalUnit == LexicalUnit.MINUS) {
            //  Rule 13 :
            System.out.println("13 ");
            childrens.add(match(LexicalUnit.MINUS));
            childrens.add(parseProd());
            childrens.add(parseExprArithPrime());
        }
        else if (
                    nextLexicalUnit == LexicalUnit.SEMI || nextLexicalUnit == LexicalUnit.EQUAL || 
                    nextLexicalUnit == LexicalUnit.SMALEQ || nextLexicalUnit == LexicalUnit.SMALLER ||
                    nextLexicalUnit == LexicalUnit.IMPLIES || nextLexicalUnit == LexicalUnit.PIPE ||
                    nextLexicalUnit == LexicalUnit.RPAREN || nextLexicalUnit == LexicalUnit.RBRACK){
                        //  Rule 14 :
                        System.out.println("14 ");
                    }
        else {
                throwParseError("Error : Unexpected token in <ExprArith'> : " + nextLexicalUnit);
        }

        return new ParseTree(retSymb, childrens);
    }

    /**
     * Parses the non-terminal {@code <Prod>} (Rule 15)
     *
     * @return The ParseTree node corresponding to {@code <Prod>}
     * @throws ParseException if any sub-step of parsing fails
     */
    public ParseTree parseProd() throws ParseException{
        System.out.println("15 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(parseAtom());
        childrens.add(parseProdPrime());

        return new ParseTree(new Symbol(NonTermUnit.Prod, "\\textless Prod\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <Prod'>}
     * 
     * @return The ParseTree node for {@code <Prod'>}
     * @throws ParseException if any sub-step of parsing fails
     */
    public ParseTree parseProdPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.Prodp, "\\textless Prod\\textquotesingle\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        //Rule 16 :
        if (nextLexicalUnit == LexicalUnit.TIMES){
            System.out.println("16 ");
            childrens.add(match(LexicalUnit.TIMES));
            childrens.add(parseAtom());
        }
        //Rule 17
        else if(nextLexicalUnit == LexicalUnit.DIVIDE){
            System.out.println("17 ");
            childrens.add(match(LexicalUnit.DIVIDE));
            childrens.add(parseAtom());
        }
        else if (   
                    nextLexicalUnit == LexicalUnit.SEMI || nextLexicalUnit == LexicalUnit.EQUAL || 
                    nextLexicalUnit == LexicalUnit.SMALEQ || nextLexicalUnit == LexicalUnit.SMALLER ||
                    nextLexicalUnit == LexicalUnit.IMPLIES || nextLexicalUnit == LexicalUnit.PIPE ||
                    nextLexicalUnit == LexicalUnit.RPAREN || nextLexicalUnit == LexicalUnit.RBRACK){
                        //  Rule 18 :
                        System.out.println("18 ");
                    }    
        return new ParseTree(retSymb, childrens);
    }

    /**
     * Parses the non-terminal {@code <Atom>}
     *
     * @return The ParseTree node for {@code <Atom>}
     * @throws ParseException if unexpected token or failure in parsing
     */
    public ParseTree parseAtom() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.Atom, "\\textless Atom\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
            
        // Rule 19 : VARNAME
        if(nextLexicalUnit == LexicalUnit.VARNAME){
            System.out.println("19 ");
            childrens.add(match(LexicalUnit.VARNAME));
        }
        // Rule 20 : NUMBER
        else if(nextLexicalUnit == LexicalUnit.NUMBER){
            System.out.println("20 ");
            childrens.add(match(LexicalUnit.NUMBER));
        }
        // Rule 21 : - <Atom>
        else if(nextLexicalUnit == LexicalUnit.MINUS){
            System.out.println("21 ");
            childrens.add(match(LexicalUnit.MINUS));
            childrens.add(parseAtom());
        }
        // Rule 22 : ( <ExprArith> )
        else if (nextLexicalUnit == LexicalUnit.LPAREN){
            System.out.println("22 ");
            childrens.add(match(LexicalUnit.LPAREN));
            childrens.add(parseExprArith());
            childrens.add(match(LexicalUnit.RPAREN));
        }
        else {throwParseError("Error : Unexpected token in <Atom> : " +  nextLexicalUnit); }

        return new ParseTree(retSymb, childrens);
    }

    /**
     * Parses the non-terminal {@code <If>} (Rule 23)
     *
     * @return The ParseTree node for {@code <If>}
     * @throws ParseException if any sub-step of parsing fails
     */
    public ParseTree parseIf() throws ParseException{
        System.out.println("23 ");

        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        childrens.add(match(LexicalUnit.IF));
        childrens.add(match(LexicalUnit.LBRACK)); 
        childrens.add(parseCond());
        childrens.add(match(LexicalUnit.RBRACK)); 
        childrens.add(match(LexicalUnit.THEN));
        childrens.add(parseCode());
        childrens.add(parseIfPrime());

        return new ParseTree(new Symbol(NonTermUnit.If, "\\textless If\\textgreater"), childrens);
    }
    
    /**
     * Parses the non-terminal {@code <If'>} : 
     *  - {@code <If'>} -> End              (Rule 24)
     *  - {@code <If'>} -> Else {@code <Code>} End  (Rule 25)
     *
     * @return The ParseTree node for {@code <If'>}
     * @throws ParseException if unexpected token of failure in parsing
     */
    public ParseTree parseIfPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.C, "\\textless C\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        if (nextLexicalUnit == LexicalUnit.END){
            System.out.println("24 ");
            childrens.add(match(LexicalUnit.END));
        }
        else if (nextLexicalUnit == LexicalUnit.ELSE){
            System.out.println("25 ");
            childrens.add(match(LexicalUnit.ELSE));
            childrens.add(parseCode());
            childrens.add(match(LexicalUnit.END));
        }
        else {throwParseError("Error : Unexpected token in <IF> : " +  nextLexicalUnit); }
        
        return new ParseTree(retSymb, childrens);
    } 

    /**
     * Parses the non-terminal {@code <Cond>} (Rule 26)
     *
     * @return The ParseTree node for {@code <Cond>}
     * @throws ParseException if parsing of {@code <CondA>} fails
     */
    public ParseTree parseCond() throws ParseException{
        System.out.println("26 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        childrens.add(parseCondA());
        return new ParseTree(new Symbol(NonTermUnit.Cond, "\\textless Cond\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <CondB>}
     *
     * @return The ParseTree node for {@code <CondB>}
     * @throws ParseException if a sub-step of parsing fails
     */
    public ParseTree parseCondB() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        if(nextLexicalUnit == LexicalUnit.IMPLIES){ //27
            System.out.println("27 ");
            childrens.add(parseCond());
        }
        else {System.out.println("28 ");} //28
        return new ParseTree(new Symbol(NonTermUnit.CondB, "\\textless CondB\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <CondA>}
     *
     * @return The ParseTree node for {@code <CondA>}
     * @throws ParseException if a sub-step of parsing fails
     */
    public ParseTree parseCondA() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.CondA, "\\textless CondA\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        if(nextLexicalUnit == LexicalUnit.PIPE){ //29
            System.out.println("29 ");
            childrens.add(parseCond());
            childrens.add(match(LexicalUnit.PIPE));
        }
        else { //30
            System.out.println("30 ");
            childrens.add(parseExprArith());
            childrens.add(parseD());
        }

        return new ParseTree(retSymb, childrens);
    }

    /**
     * Parses the non-terminal {@code <D>}
     *
     * @return The ParseTree node for {@code <D>}
     * @throws ParseException if unexpected token or failure in sub-parsing
     */
    public ParseTree parseD() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.D, "\\textless D\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        
        if (nextLexicalUnit == LexicalUnit.EQUAL){
            System.out.println("31 ");
            childrens.add(match(LexicalUnit.EQUAL));
            childrens.add(parseExprArith());
        } else if (nextLexicalUnit == LexicalUnit.SMALEQ){
            System.out.println("32 ");
            childrens.add(match(LexicalUnit.SMALEQ));
            childrens.add(parseExprArith());
        }else if (nextLexicalUnit == LexicalUnit.SMALLER){
            System.out.println("33 ");
            childrens.add(match(LexicalUnit.SMALLER));
            childrens.add(parseExprArith());
        }
        else {throwParseError("Error : Unexpected token in <D> : " +  nextLexicalUnit); }

        return new ParseTree(retSymb, childrens);
    }

    /**
     * Parses the non-terminal {@code <While>} (rule 34)
     *
     * @return The ParseTree node for {@code <While>}
     * @throws ParseException if any sub-step of parsing fails
     */
    public ParseTree parseWhile() throws ParseException{
        System.out.println("34 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(match(LexicalUnit.WHILE));
        childrens.add(match(LexicalUnit.LBRACK));
        childrens.add(parseCond());
        childrens.add(match(LexicalUnit.RBRACK));
        childrens.add(match(LexicalUnit.DO));
        childrens.add(parseCode());
        childrens.add(match(LexicalUnit.END));

        return new ParseTree(new Symbol(NonTermUnit.While, "\\textless While\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <Output>} (Rule 35)
     *
     * @return The ParseTree node for {@code <Output>}
     * @throws ParseException if any sub-step of parsing fails
     */
    public ParseTree parseOutput() throws ParseException{
        System.out.println("35 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(match(LexicalUnit.PRINT));
        childrens.add(match(LexicalUnit.LPAREN));
        childrens.add(match(LexicalUnit.VARNAME));
        childrens.add(match(LexicalUnit.RPAREN));

        return new ParseTree(new Symbol(NonTermUnit.Output, "\\textless Output\\textgreater"), childrens);
    }

    /**
     * Parses the non-terminal {@code <Input>} (Rule 36)
     *
     * @return The ParseTree node for {@code <Input>}
     * @throws ParseException if any sub-step of parsing fails
     */
    public ParseTree parseInput() throws ParseException{
        System.out.println("36 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(match(LexicalUnit.INPUT));
        childrens.add(match(LexicalUnit.LPAREN));
        childrens.add(match(LexicalUnit.VARNAME));
        childrens.add(match(LexicalUnit.RPAREN));

        return new ParseTree(new Symbol(NonTermUnit.Input, "\\textless Input\\textgreater"), childrens);
    }

    /**
     * Reads lexunits and values from a text file and stores them in a Pair. 
     *
     * @param filename The name of the text file in which to read
     * @return The Pair containing a List of LexicalUnit and a List of String (values)
     * @throws Exception if error reading file or unknown lexical unit
     */
    private static Pair<List<LexicalUnit>, List<String>> readLexicalUnitsFromFile(String filename) throws Exception {
        List<LexicalUnit> lexunits = new java.util.ArrayList<>();
        List<String> tokens = new java.util.ArrayList<>();
        tokenRawLines.clear();
        tokenLineNumbers.clear();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.FileReader(filename))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Format: "token: XXX \t lexical unit: YYYY"
                String[] parts = line.split("lexical unit:");
                if (parts.length >= 2) {
                    String lexicalUnitStr = parts[1].trim();
                    LexicalUnit lexunit = null;
                    String value = parts[0].trim().split("token:")[1].trim();
                    try {
                        lexunit = LexicalUnit.valueOf(lexicalUnitStr.toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        System.err.println("ERROR: Unknown lexical unit: " + lexicalUnitStr);
                        lexunit = null;
                    }
                    if (lexunit != null) {
                        lexunits.add(lexunit);
                        tokens.add(value);
                    }
                }
                tokenRawLines.add(line);
                
                int srcLine = -1;
                String lower = line.toLowerCase();
                if (lower.startsWith("line:")) {
                    String rest = line.substring(lower.indexOf(":")+1).trim();
                    String[] partsForLine = rest.split("\\s+", 2);
                    try { srcLine = Integer.parseInt(partsForLine[0]); } catch (Exception e) { srcLine = -1; }
                }
                tokenLineNumbers.add(srcLine);
            }
        }
        
        // Add EOS at the end
        lexunits.add(LexicalUnit.EOS);
        tokenRawLines.add("EOS");
        tokenLineNumbers.add(-1);

        return new Pair<List<LexicalUnit>, List<String>>(lexunits, tokens);
    }

    /**
     * Writes the LaTeX code to a tex file, that can be compiled to make a pdf of the derivation tree
     *
     * @param filepath the path to the tex file
     * @throws FileNotFoundException if the file doesn't exist
     */
    public void buildTree(String filepath) throws FileNotFoundException {
        String latexCode = tree.toLaTeX();
        PrintStream latexFile = new PrintStream(new File(filepath));
        PrintStream console = System.out;
        System.setOut(latexFile);
        System.out.println(latexCode);
        System.setOut(console);
    }

    /**
     * Returns the parse tree generated after parsing
     *
     * @return The parse tree root node
     */
    public ParseTree getParseTree() {
        return this.tree;
    }
}

