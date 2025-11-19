package Parser;

import java.util.List;
import java.util.Arrays;
import java.io.*;
import LexicalAnalyzer.LexicalUnit;
import LexicalAnalyzer.Symbol;
import LexicalAnalyzer.NonTermUnit;

//Un parser a une liste de lexunits et un index de la position actuelle
public class Parser{
    private List<LexicalUnit> lexunits;
    private List<String> tokens;
    private int currentLexicalUnitIndex;
    private static java.util.List<String> tokenRawLines = new java.util.ArrayList<>();
    private static java.util.List<Integer> tokenLineNumbers = new java.util.ArrayList<>();
    private ParseTree tree;

    public Parser() throws Exception {
        Pair<List<LexicalUnit>, List<String>> res = readLexicalUnitsFromFile("test/LexicalAnalyzerOutput.txt");
        this.lexunits = res.getFirst();
        this.tokens = res.getSecond();
        this.currentLexicalUnitIndex = 0;
    }

    private void throwParseError(String message) throws ParseException {
        int idx = currentLexicalUnitIndex;
        String raw = "n/a";
        int srcLine = -1;
        if (tokenRawLines != null && idx >= 0 && idx < tokenRawLines.size()) raw = tokenRawLines.get(idx);
        if (tokenLineNumbers != null && idx >= 0 && idx < tokenLineNumbers.size()) srcLine = tokenLineNumbers.get(idx);
        String loc = srcLine > 0 ? "line " + srcLine : "token index " + idx;
        throw new ParseException(message + " at " + loc + " -> " + raw);
    }
    //Regarde le token actuel
    private LexicalUnit lookCurrent(){
        if (currentLexicalUnitIndex < lexunits.size()){
            return lexunits.get(currentLexicalUnitIndex);
        }
        return LexicalUnit.EOS;
    }

    //  On consomme le token actuel, s\textquotesingleil correspond à ce qui est attendu on 
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

    //  POINT D\textquotesingleENTREE
    public void startParsing() throws ParseException {
        tree = parseProgram();
    }

    //  Production 1 : 
    //  <Program> -> Prog [ProgName] Is <Code> End
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


    //  NON-TERMINAUX
    //  Productions 2 & 3 :
    //  <Code> -> <Instruction>
    //  <Code> | epsilon
    public ParseTree parseCode() throws ParseException {
        System.out.println("2 ");
        LexicalUnit nextLexicalUnit = lookCurrent();
        //  Règle 2 :
        // First¹((Instruction); (Code) Follow¹(Code))) = {[VarName], If, While, Print, Input} [cite: 8]
        if (nextLexicalUnit == LexicalUnit.VARNAME || nextLexicalUnit == LexicalUnit.IF ||
            nextLexicalUnit == LexicalUnit.WHILE || nextLexicalUnit == LexicalUnit.PRINT || nextLexicalUnit == LexicalUnit.INPUT){
                List<ParseTree> childrens = new java.util.ArrayList<>(); 
                childrens.add(parseInstruction());
                childrens.add(match(LexicalUnit.SEMI));
                childrens.add(parseCode());
                return new ParseTree(new Symbol(NonTermUnit.Code, "\\textless Code\\textgreater"), childrens);
        }

        // Règle 3 : <Code> -> epsilon
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

    //  Productions 4 à 8 :

    public ParseTree parseInstruction() throws ParseException{
        
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.Instruction, "\\textless Instruction\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        
        if (nextLexicalUnit == LexicalUnit.VARNAME) {
            // Règle 4 : [VarName] (Implique <Assign>)
            System.out.println("4 ");
            childrens.add(parseAssign());

        }
        else if (nextLexicalUnit == LexicalUnit.IF){
            // Règle 5 : If [cite: 12]
            System.out.println("5 ");
            childrens.add(parseIf());
        }
        else if (nextLexicalUnit == LexicalUnit.WHILE){
            // Règle 6 : While [cite: 13]
            System.out.println("6 ");
            childrens.add(parseWhile());
        }
        else if (nextLexicalUnit == LexicalUnit.PRINT){
            // Règle 7 : Print (Implique <Output>) [cite: 14]
            System.out.println("7 ");
            childrens.add(parseOutput());
        }
        else if (nextLexicalUnit == LexicalUnit.INPUT){
            // Règle 8 : Input (Implique <Input>) [cite: 15]
            System.out.println("8 ");
            childrens.add(parseInput());
        }
        return new ParseTree(retSymb, childrens);
    }

    // Règle 10 : <Assign> -> [VarName] = <ExprArith>
    public ParseTree parseAssign() throws ParseException{    
        System.out.println("10 ");

        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        childrens.add(match(LexicalUnit.VARNAME));
        childrens.add(match(LexicalUnit.ASSIGN));
        childrens.add(parseExprArith());
        
        return new ParseTree(new Symbol(NonTermUnit.Assign, "\\textless Assign\\textgreater"), childrens);
    }

    // Règle 11 : ⟨P rod⟩⟨ExprArith′⟩ · F ollow1(⟨ExprArith⟩=n[V arN ame], [Number], −,(
    public ParseTree parseExprArith() throws ParseException{
        System.out.println("10 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(parseProd());
        childrens.add(parseExprArithPrime());

        return new ParseTree(new Symbol(NonTermUnit.ExprArith, "\\textless ExprArith\\textgreater"), childrens);
    }

    public ParseTree parseExprArithPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.ExprArithp, "\\textless ExprArith\\textquotesingle\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        if (nextLexicalUnit == LexicalUnit.PLUS) {
            // Règle 12 :
            System.out.println("12 ");
            childrens.add(match(LexicalUnit.PLUS));
            childrens.add(parseProd());
            childrens.add(parseExprArithPrime());
        }
        else if (nextLexicalUnit == LexicalUnit.MINUS) {
            //  Règle 13 :
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
                        //  Règle 14 :
                        System.out.println("14 ");
                    }
        else {
                throwParseError("Error : Unexpected token in <ExprArith'> : " + nextLexicalUnit);
        }

        return new ParseTree(retSymb, childrens);
    }

    // Règle 15 :
    public ParseTree parseProd() throws ParseException{
        System.out.println("15 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(parseAtom());
        childrens.add(parseProdPrime());

        return new ParseTree(new Symbol(NonTermUnit.Prod, "\\textless Prod\\textgreater"), childrens);
    }

    public ParseTree parseProdPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.Prodp, "\\textless Prod\\textquotesingle\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        //Règle 16 :
        if (nextLexicalUnit == LexicalUnit.TIMES){
            System.out.println("16 ");
            childrens.add(match(LexicalUnit.TIMES));
            childrens.add(parseAtom());
        }
        //Règle 17
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
                        //  Règle 18 :
                        System.out.println("18 ");
                    }    
        return new ParseTree(retSymb, childrens);
    }

    public ParseTree parseAtom() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        Symbol retSymb = new Symbol(NonTermUnit.Atom, "\\textless Atom\\textgreater");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
            
        // Règle 19 : VARNAME
        if(nextLexicalUnit == LexicalUnit.VARNAME){
            System.out.println("19 ");
            childrens.add(match(LexicalUnit.VARNAME));
        }
        // Règle 20 : NUMBER
        else if(nextLexicalUnit == LexicalUnit.NUMBER){
            System.out.println("20 ");
            childrens.add(match(LexicalUnit.NUMBER));
        }
        // Règle 21 : - <Atom>
        else if(nextLexicalUnit == LexicalUnit.MINUS){
            System.out.println("21 ");
            childrens.add(match(LexicalUnit.MINUS));
            childrens.add(parseAtom());
        }
        // Règle 22 : ( <ExprArith> )
        else if (nextLexicalUnit == LexicalUnit.LPAREN){
            System.out.println("22 ");
            childrens.add(match(LexicalUnit.LPAREN));
            childrens.add(parseExprArith());
            childrens.add(match(LexicalUnit.RPAREN));
        }
        else {throwParseError("Error : Unexpected token in <Atom> : " +  nextLexicalUnit); }

        return new ParseTree(retSymb, childrens);
    }

    //  Règle 23
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
    
    //  Règles 24 - 25 :
    // Parse If\textquotesingle -> End
    // Parse If\textquotesingle -> Else <Code> End
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

    // règle 26
    public ParseTree parseCond() throws ParseException{
        System.out.println("26 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 
        childrens.add(parseCondA());
        return new ParseTree(new Symbol(NonTermUnit.Cond, "\\textless Cond\\textgreater"), childrens);
    }

    public void parseCondB() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();

        if(nextLexicalUnit == LexicalUnit.IMPLIES){ //27
            System.out.println("27 ");
            parseCond();
        }
        else {System.out.println("28 ");} //28
    }

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

    //31-33
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

    //Règle 34
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

    //Règle 35
    public ParseTree parseOutput() throws ParseException{
        System.out.println("35 ");
        List<ParseTree> childrens = new java.util.ArrayList<>(); 

        childrens.add(match(LexicalUnit.PRINT));
        childrens.add(match(LexicalUnit.LPAREN));
        childrens.add(match(LexicalUnit.VARNAME));
        childrens.add(match(LexicalUnit.RPAREN));

        return new ParseTree(new Symbol(NonTermUnit.Output, "\\textless Output\\textgreater"), childrens);
    }

    //Règle 36
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
     * Lit les lexunits depuis un fichier au format output.txt
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
                        LexicalUnit lu = LexicalUnit.valueOf(lexicalUnitStr.toUpperCase());
                        lexunit = convertLexicalUnitToLexicalUnit(lu);
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
        
        // Ajouter EOS à la fin
        lexunits.add(LexicalUnit.EOS);
        tokenRawLines.add("EOS");
        tokenLineNumbers.add(-1);

        return new Pair<List<LexicalUnit>, List<String>>(lexunits, tokens);
    }

    /**
     * Convertit une unité lexicale (string) en LexicalUnit enum
     */
    private static LexicalUnit convertLexicalUnitToLexicalUnit(LexicalUnit lexicalUnit) {
        switch (lexicalUnit) {
            case PROG: return LexicalUnit.PROG;
            case PROGNAME: return LexicalUnit.PROGNAME;
            case IS: return LexicalUnit.IS;
            case END: return LexicalUnit.END;
            case SEMI: return LexicalUnit.SEMI;
            case ASSIGN: return LexicalUnit.ASSIGN;
            case PLUS: return LexicalUnit.PLUS;
            case MINUS: return LexicalUnit.MINUS;
            case TIMES: return LexicalUnit.TIMES;
            case DIVIDE: return LexicalUnit.DIVIDE;
            case VARNAME: return LexicalUnit.VARNAME;
            case NUMBER: return LexicalUnit.NUMBER;
            case LPAREN: return LexicalUnit.LPAREN;
            case RPAREN: return LexicalUnit.RPAREN;
            case IMPLIES: return LexicalUnit.IMPLIES;
            case EQUAL: return LexicalUnit.EQUAL;
            case SMALEQ: return LexicalUnit.SMALEQ;
            case SMALLER: return LexicalUnit.SMALLER;
            case WHILE: return LexicalUnit.WHILE;
            case DO: return LexicalUnit.DO;
            case IF: return LexicalUnit.IF;
            case PRINT: return LexicalUnit.PRINT;
            case INPUT: return LexicalUnit.INPUT;
            case LBRACK: return LexicalUnit.LBRACK;
            case RBRACK: return LexicalUnit.RBRACK;
            case THEN: return LexicalUnit.THEN;
            case ELSE: return LexicalUnit.ELSE;
            case PIPE: return LexicalUnit.PIPE;
            default:
                System.err.println("ERROR: Unknown Symbol: " + lexicalUnit);
                return null;
        }
    }

    public void buildTree(String filepath) throws FileNotFoundException {
        String latexCode = tree.toLaTeX();
        PrintStream latexFile = new PrintStream(new File(filepath));
        PrintStream console = System.out;
        System.setOut(latexFile);
        System.out.println(latexCode);
        System.setOut(console);
    }
}

