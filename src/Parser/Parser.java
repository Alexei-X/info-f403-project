package Parser;

import java.util.List;
import java.util.Arrays;
import LexicalAnalyzer.LexicalUnit;

//Un parser a une liste de tokens et un index de la position actuelle
public class Parser{
    private List<LexicalUnit> tokens;
    private int currentLexicalUnitIndex;
    private static java.util.List<String> tokenRawLines = new java.util.ArrayList<>();
    private static java.util.List<Integer> tokenLineNumbers = new java.util.ArrayList<>();

    public Parser() throws Exception {
        this.tokens = readLexicalUnitsFromFile("test/LexicalAnalyzerOutput.txt");
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
        if (currentLexicalUnitIndex < tokens.size()){
            return tokens.get(currentLexicalUnitIndex);
        }
        return LexicalUnit.EOS;
    }

    //  On consomme le token actuel, s'il correspond à ce qui est attendu on 
    private void match(LexicalUnit expected) throws ParseException {
        if (lookCurrent() == expected) {
            currentLexicalUnitIndex++;
        } else{
            throwParseError("Error: Expected " + expected + " got " + lookCurrent());
        } 
    }

    //  POINT D'ENTREE
    //  Production 1 : 
    //  <Program> -> Prog [ProgName] Is <Code> End
    public void parseProgram() throws ParseException {
        System.out.println("1 ");

        match(LexicalUnit.PROG);
        match(LexicalUnit.PROGNAME);
        match(LexicalUnit.IS);
        parseCode();
        match(LexicalUnit.END);

        if (lookCurrent() != LexicalUnit.EOS){
            throwParseError("Unexpected token after end of program");
        }
    }


    //  NON-TERMINAUX
    //  Productions 2 & 3 :
    //  <Code> -> <Instruction>
    //  <Code> | epsilon
    public void parseCode() throws ParseException {
        System.out.println("2 ");
        LexicalUnit nextLexicalUnit = lookCurrent();
        //  Règle 2 :
        // First¹((Instruction); (Code) Follow¹(Code))) = {[VarName], If, While, Print, Input} [cite: 8]
        if (nextLexicalUnit == LexicalUnit.VARNAME || nextLexicalUnit == LexicalUnit.IF ||
            nextLexicalUnit == LexicalUnit.WHILE || nextLexicalUnit == LexicalUnit.PRINT || nextLexicalUnit == LexicalUnit.INPUT){
                
                parseInstruction();
                match(LexicalUnit.SEMI);
                parseCode();
            }

        // Règle 3 : <Code> -> epsilon
        // Follow¹(<Code>) = {End, Else} [cite: 10]
        else if (nextLexicalUnit == LexicalUnit.END || nextLexicalUnit == LexicalUnit.ELSE){
            System.out.println("3 ");
        }
        
        else {
            throwParseError("Error : Unexpected token in <Code> : " + nextLexicalUnit);
        }
    }

    //  Productions 4 à 8 :

    public void parseInstruction() throws ParseException{
        
        LexicalUnit nextLexicalUnit = lookCurrent();
        
        if (nextLexicalUnit == LexicalUnit.VARNAME) {
            // Règle 4 : [VarName] (Implique <Assign>)
            System.out.println("4 ");
            parseAssign();
        }
        else if (nextLexicalUnit == LexicalUnit.IF){
            // Règle 5 : If [cite: 12]
            System.out.println("5 ");
            parseIf();
        }
        else if (nextLexicalUnit == LexicalUnit.WHILE){
            // Règle 6 : While [cite: 13]
            System.out.println("6 ");
            parseWhile();
        }
        else if (nextLexicalUnit == LexicalUnit.PRINT){
            // Règle 7 : Print (Implique <Output>) [cite: 14]
            System.out.println("7 ");
            parseOutput();
        }
        else if (nextLexicalUnit == LexicalUnit.INPUT){
            // Règle 8 : Input (Implique <Input>) [cite: 15]
            System.out.println("8 ");
            parseInput();
        }
    }

    // Règle 10 : <Assign> -> [VarName] = <ExprArith>
    public void parseAssign() throws ParseException{    
        System.out.println("10 ");

        match(LexicalUnit.VARNAME);
        match(LexicalUnit.ASSIGN);
        parseExprArith();
    }

    // Règle 10 : ⟨P rod⟩⟨ExprArith′⟩ · F ollow1(⟨ExprArith⟩=n[V arN ame], [Number], −,(
    public void parseExprArith() throws ParseException{
        System.out.println("10 ");

        parseProd();
        parseExprArithPrime();
    }

    public void parseExprArithPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();

        if (nextLexicalUnit == LexicalUnit.PLUS) {
            // Règle 12 :
            System.out.println("12 ");
            match(LexicalUnit.PLUS);
            parseProd();
            parseExprArithPrime();
        }
        else if (nextLexicalUnit == LexicalUnit.MINUS) {
            //  Règle 13 :
            System.out.println("13 ");
            match(LexicalUnit.MINUS);
            parseProd();
            parseExprArithPrime();
        }
        else if (   // l'enfer MDRRRRRRRRR
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
    }

    // Règle 15 :
    public void parseProd() throws ParseException{
        System.out.println("15 ");

        parseAtom();
        parseProdPrime();
    }

    public void parseProdPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();

        //Règle 16 :
        if (nextLexicalUnit == LexicalUnit.TIMES){
            System.out.println("16 ");
            parseAtom();
        }
        //Règle 17
        else if(nextLexicalUnit == LexicalUnit.DIVIDE){
            System.out.println("17 ");
            parseAtom();
        }
        else if (   
                    nextLexicalUnit == LexicalUnit.SEMI || nextLexicalUnit == LexicalUnit.EQUAL || 
                    nextLexicalUnit == LexicalUnit.SMALEQ || nextLexicalUnit == LexicalUnit.SMALLER ||
                    nextLexicalUnit == LexicalUnit.IMPLIES || nextLexicalUnit == LexicalUnit.PIPE ||
                    nextLexicalUnit == LexicalUnit.RPAREN || nextLexicalUnit == LexicalUnit.RBRACK){
                        //  Règle 18 :
                        System.out.println("18 ");
                    }    
    }

    public void parseAtom() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
            
        // Règle 19 : VARNAME
        if(nextLexicalUnit == LexicalUnit.VARNAME){
            System.out.println("19 ");
            match(LexicalUnit.VARNAME);
        }
        // Règle 20 : NUMBER
        else if(nextLexicalUnit == LexicalUnit.NUMBER){
            System.out.println("20 ");
            match(LexicalUnit.NUMBER);
        }
        // Règle 21 : - <Atom>
        else if(nextLexicalUnit == LexicalUnit.MINUS){
            System.out.println("21 ");
            match(LexicalUnit.MINUS);
            parseAtom();
        }
        // Règle 22 : ( <ExprArith> )
        else if (nextLexicalUnit == LexicalUnit.LPAREN){
            System.out.println("22 ");
            match(LexicalUnit.LPAREN);
            parseExprArith();
            match(LexicalUnit.RPAREN);
        }
        else {throwParseError("Error : Unexpected token in <Atom> : " +  nextLexicalUnit); }
    }

    //  Règle 23
    public void parseIf() throws ParseException{
        System.out.println("23 ");

        match(LexicalUnit.IF);
        match(LexicalUnit.LBRACK); 
        parseCond();
        match(LexicalUnit.RBRACK); 
        match(LexicalUnit.THEN);
        parseCode();
        parseIfPrime();
    }
    
    //  Règles 24 - 25 :
    // Parse If' -> End
    // Parse If' -> Else <Code> End
    public void parseIfPrime() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();

        if (nextLexicalUnit == LexicalUnit.END){
            System.out.println("24 ");
            match(LexicalUnit.END);
        }
        else if (nextLexicalUnit == LexicalUnit.ELSE){
            System.out.println("25 ");
            match(LexicalUnit.ELSE);
            parseCode();
            match(LexicalUnit.END);
        }
        else {throwParseError("Error : Unexpected token in <IF> : " +  nextLexicalUnit); }
    } 

    // règle 26
    public void parseCond() throws ParseException{
        System.out.println("26 ");

        parseCondA();
    }

    public void parseCondB() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();

        if(nextLexicalUnit == LexicalUnit.IMPLIES){ //27
            System.out.println("27 ");
            parseCond();
        }
        else {System.out.println("28 ");} //28
    }

    public void parseCondA() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();

        if(nextLexicalUnit == LexicalUnit.PIPE){ //29
            System.out.println("29 ");
            parseCond();
            match(LexicalUnit.PIPE);
        }
        else { //30
            System.out.println("30 ");
            parseExprArith();
            parseD();
        }
    }

    //31-33
    public void parseD() throws ParseException{
        LexicalUnit nextLexicalUnit = lookCurrent();
        
        if (nextLexicalUnit == LexicalUnit.EQUAL){
            System.out.println("31 ");
            match(LexicalUnit.EQUAL);
            parseExprArith();
        } else if (nextLexicalUnit == LexicalUnit.SMALEQ){
            System.out.println("32 ");
            match(LexicalUnit.SMALEQ);
            parseExprArith();
        }else if (nextLexicalUnit == LexicalUnit.SMALLER){
            System.out.println("33 ");
            match(LexicalUnit.SMALLER);
            parseExprArith();
        }
        else {throwParseError("Error : Unexpected token in <D> : " +  nextLexicalUnit); }
    }

    //Règle 34
    public void parseWhile() throws ParseException{
        System.out.println("34 ");

        match(LexicalUnit.WHILE);
        match(LexicalUnit.LBRACK);
        parseCond();
        match(LexicalUnit.RBRACK);
        match(LexicalUnit.DO);
        parseCode();
        match(LexicalUnit.END);
    }

    //Règle 35
    public void parseOutput() throws ParseException{
        System.out.println("35 ");

        match(LexicalUnit.PRINT);
        match(LexicalUnit.LPAREN);
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.RPAREN);
    }

    //Règle 36
    public void parseInput() throws ParseException{
        System.out.println("36 ");

        match(LexicalUnit.INPUT);
        match(LexicalUnit.LPAREN);
        match(LexicalUnit.VARNAME);
        match(LexicalUnit.RPAREN);
    }

    /**
     * Lit les tokens depuis un fichier au format output.txt
     */
    private static List<LexicalUnit> readLexicalUnitsFromFile(String filename) throws Exception {
        List<LexicalUnit> tokens = new java.util.ArrayList<>();
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
                    LexicalUnit token = null;
                    try {
                        LexicalUnit lu = LexicalUnit.valueOf(lexicalUnitStr.toUpperCase());
                        token = convertLexicalUnitToLexicalUnit(lu);
                    } catch (IllegalArgumentException iae) {
                        System.err.println("ERROR: Unknown lexical unit: " + lexicalUnitStr);
                        token = null;
                    }
                    if (token != null) {
                        tokens.add(token);
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
        tokens.add(LexicalUnit.EOS);
        tokenRawLines.add("EOS");
        tokenLineNumbers.add(-1);
        
        return tokens;
    }

    /**
     * Convertit une unité lexicale (string) en LexicalUnit enum
     */
    private static LexicalUnit convertLexicalUnitToLexicalUnit(LexicalUnit lexicalUnit) {
        switch (lexicalUnit) {
            case PROG: return LexicalUnit.PROG;
            case PROGNAME: return LexicalUnit.PROGNAME; // PROGNAME est traité comme VARNAME
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
}

