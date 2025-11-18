package Parser;

import java.util.List;
import java.util.Arrays;

//Un parser a une liste de tokens et un index de la position actuelle
public class Parser{
    private List<Token> tokens;
    private int currentTokenIndex;
    private static java.util.List<String> tokenRawLines = new java.util.ArrayList<>();
    private static java.util.List<Integer> tokenLineNumbers = new java.util.ArrayList<>();

    public Parser(List<Token> tokens){
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    private void throwParseError(String message) throws ParseException {
        int idx = currentTokenIndex;
        String raw = "n/a";
        int srcLine = -1;
        if (tokenRawLines != null && idx >= 0 && idx < tokenRawLines.size()) raw = tokenRawLines.get(idx);
        if (tokenLineNumbers != null && idx >= 0 && idx < tokenLineNumbers.size()) srcLine = tokenLineNumbers.get(idx);
        String loc = srcLine > 0 ? "line " + srcLine : "token index " + idx;
        throw new ParseException(message + " at " + loc + " -> " + raw);
    }
    //Regarde le token actuel
    private Token lookCurrent(){
        if (currentTokenIndex < tokens.size()){
            return tokens.get(currentTokenIndex);
        }
        return Token.EOF;
    }

    //  On consomme le token actuel, s'il correspond à ce qui est attendu on 
    private void match(Token expected) throws ParseException {
        if (lookCurrent() == expected) {
            System.out.println("Matched: " + expected);
            currentTokenIndex++;
        } else{
            throwParseError("Error: Expected " + expected + " got " + lookCurrent());
        } 
    }

    //  POINT D'ENTREE
    //  Production 1 : 
    //  <Program> -> Prog [ProgName] Is <Code> End
    public void parseProgram() throws ParseException {
        System.out.println("Parsing <Program>");

        match(Token.PROG);
        match(Token.VAR_NAME);
        match(Token.IS);
        parseCode();
        match(Token.END);

        if (lookCurrent() != Token.EOF){
            throwParseError("Unexpected token after end of program");
        }
        System.out.println("Parsed <Program> successfully");
    }


    //  NON-TERMINAUX
    //  Productions 2 & 3 :
    //  <Code> -> <Instruction>
    //  <Code> | epsilon
    public void parseCode() throws ParseException {
        System.out.println("Parsing <Code>");

        Token nextToken = lookCurrent();
        //  Règle 2 :
        // First¹((Instruction); (Code) Follow¹(Code))) = {[VarName], If, While, Print, Input} [cite: 8]
        if (nextToken == Token.VAR_NAME || nextToken == Token.IF ||
            nextToken == Token.WHILE || nextToken == Token.PRINT || nextToken == Token.INPUT){
                
                parseInstruction();
                match(Token.SEMICOLON);
                parseCode();
            }

        // Règle 3 : <Code> -> epsilon
        // Follow¹(<Code>) = {End, Else} [cite: 10]
        else if (nextToken == Token.END || nextToken == Token.ELSE){
            System.out.println("Epsilon for <Code>");
        }
        
        else {
            throwParseError("Error : Unexpected token in <Code> : " + nextToken);
        }
    }

    //  Productions 4 à 8 :

    public void parseInstruction() throws ParseException{
        System.out.println("Parsing <Instruction>");
        Token nextToken = lookCurrent();
        
        if (nextToken == Token.VAR_NAME) {
            // Règle 4 : [VarName] (Implique <Assign>)
            parseAssign();
        }
        else if (nextToken == Token.IF){
            // Règle 5 : If [cite: 12]
            parseIf();
        }
        else if (nextToken == Token.WHILE){
            // Règle 6 : While [cite: 13]
            parseWhile();
        }
        else if (nextToken == Token.PRINT){
            // Règle 7 : Print (Implique <Output>) [cite: 14]
            parseOutput();
        }
        else if (nextToken == Token.INPUT){
            // Règle 8 : Input (Implique <Input>) [cite: 15]
            parseInput();
        }
    }

    // Règle 10 : <Assign> -> [VarName] = <ExprArith>
    public void parseAssign() throws ParseException{    
        System.out.println("Parsing <Assign>");

        match(Token.VAR_NAME);
        match(Token.ASSIGN);
        parseExprArith();
    }

    // Règle 10 : ⟨P rod⟩⟨ExprArith′⟩ · F ollow1(⟨ExprArith⟩=n[V arN ame], [Number], −,(
    public void parseExprArith() throws ParseException{
        System.out.println("Parsing <ExprArith>");

        parseProd();
        parseExprArithPrime();
    }

    public void parseExprArithPrime() throws ParseException{
        System.out.println("Parsing <ExprArith'>");
        Token nextToken = lookCurrent();

        if (nextToken == Token.PLUS) {
            // Règle 12 :
            match(Token.PLUS);
            parseProd();
            parseExprArithPrime();
        }
        else if (nextToken == Token.MINUS) {
            //  Règle 13 :
            match(Token.MINUS);
            parseProd();
            parseExprArithPrime();
        }
        else if (   // l'enfer MDRRRRRRRRR
                    nextToken == Token.SEMICOLON || nextToken == Token.EQUAL_EQUAL || 
                    nextToken == Token.LESS_EQUAL || nextToken == Token.LESS ||
                    nextToken == Token.ARROW || nextToken == Token.PIPE ||
                    nextToken == Token.CLOSE_PAREN || nextToken == Token.CLOSE_CURLY){
                        //  Règle 14 :
                        System.out.println("Epsilon for <ExprArith'>"); // Tout ça pour ne rien faire 
                    }
        else {
                throwParseError("Error : Unexpected token in <ExprArith'> : " + nextToken);
        }
    }

    // Règle 15 :
    public void parseProd() throws ParseException{
        System.out.println("Parsing <Prod>");

        parseAtom();
        parseProdPrime();
    }

    public void parseProdPrime() throws ParseException{
        System.out.println("Parsing <Prod'>");
        Token nextToken = lookCurrent();

        //Règle 16 :
        if (nextToken == Token.MULT){
            parseAtom();
        }
        //Règle 17
        else if(nextToken == Token.DIV){
            parseAtom();
        }
        else if (   
                    nextToken == Token.SEMICOLON || nextToken == Token.EQUAL_EQUAL || 
                    nextToken == Token.LESS_EQUAL || nextToken == Token.LESS ||
                    nextToken == Token.ARROW || nextToken == Token.PIPE ||
                    nextToken == Token.CLOSE_PAREN || nextToken == Token.CLOSE_CURLY){
                        //  Règle 18 :
                        System.out.println("Epsilon for <Prod'>"); 
                    }    
    }

    public void parseAtom() throws ParseException{
        System.out.println("Parsing <Atom>");
        Token nextToken = lookCurrent();
            
        // Règle 19 : VAR_NAME
        if(nextToken == Token.VAR_NAME){
            match(Token.VAR_NAME);
        }
        // Règle 20 : NUMBER
        else if(nextToken == Token.NUMBER){
            match(Token.NUMBER);
        }
        // Règle 21 : - <Atom>
        else if(nextToken == Token.MINUS){
            match(Token.MINUS);
            parseAtom();
        }
        // Règle 22 : ( <ExprArith> )
        else if (nextToken == Token.OPEN_PAREN){
            match(Token.OPEN_PAREN);
            parseExprArith();
            match(Token.CLOSE_PAREN);
        }
        else {throwParseError("Error : Unexpected token in <Atom> : " +  nextToken); }
    }

    //  Règle 23
    public void parseIf() throws ParseException{
        System.out.println("Parsing <If>");

        match(Token.IF);
        match(Token.OPEN_CURLY); 
        parseCond();
        match(Token.CLOSE_CURLY); 
        match(Token.THEN);
        parseCode();
        parseIfPrime();
    }
    
    //  Règles 24 - 25 :
    // Parse If' -> End
    // Parse If' -> Else <Code> End
    public void parseIfPrime() throws ParseException{
        System.out.println("parsing If'");
        Token nextToken = lookCurrent();

        if (nextToken == Token.END){
            match(Token.END);
        }
        else if (nextToken == Token.ELSE){
            match(Token.ELSE);
            parseCode();
            match(Token.END);
        }
        else {throwParseError("Error : Unexpected token in <IF> : " +  nextToken); }
    } 

    // règle 26
    public void parseCond() throws ParseException{
        System.out.println("Parsing <Cond>");

        parseCondA();
    }

    public void parseCondB() throws ParseException{
        System.out.println("Parsing <CondB>");
        Token nextToken = lookCurrent();

        if(nextToken == Token.ARROW){ //27
            parseCond();
        }
        else {} //28
    }

    public void parseCondA() throws ParseException{
        System.out.println("Parsing <CondA>");
        Token nextToken = lookCurrent();

        if(nextToken == Token.PIPE){ //29
            parseCond();
            match(Token.PIPE);
        }
        else { //30
            parseExprArith();
            parseD();
        }
    }

    //31-33
    public void parseD() throws ParseException{
        System.out.println("Parsing <D>");
        Token nextToken = lookCurrent();
        
        if (nextToken == Token.EQUAL_EQUAL){
            match(Token.EQUAL_EQUAL);
            parseExprArith();
        } else if (nextToken == Token.LESS_EQUAL){
            match(Token.LESS_EQUAL);
            parseExprArith();
        }else if (nextToken == Token.LESS){
            match(Token.LESS);
            parseExprArith();
        }
        else {throwParseError("Error : Unexpected token in <D> : " +  nextToken); }
    }

    //Règle 34
    public void parseWhile() throws ParseException{
        System.out.println("Parsing <while>");

        match(Token.WHILE);
        match(Token.OPEN_CURLY);
        parseCond();
        match(Token.CLOSE_CURLY);
        match(Token.DO);
        parseCode();
        match(Token.END);
    }

    //Règle 35
    public void parseOutput() throws ParseException{
        System.out.println("Parsing <Output>");

        match(Token.PRINT);
        match(Token.OPEN_PAREN);
        match(Token.VAR_NAME);
        match(Token.CLOSE_PAREN);
    }

    //Règle 36
    public void parseInput() throws ParseException{
        System.out.println("Parsing <Input>");

        match(Token.INPUT);
        match(Token.OPEN_PAREN);
        match(Token.VAR_NAME);
        match(Token.CLOSE_PAREN);
    }


    //Main calls the parsing process
    public static void main(String[] args){
        try {
            // Lire les tokens depuis LexicalAnalyzerOutput.txt
            List<Token> tokens = readTokensFromFile("test/LexicalAnalyzerOutput.txt");
            
            System.out.println("Readen Tokens: " + tokens.size());
            
            // Créer le parser avec la liste de tokens
            Parser parser = new Parser(tokens);
            
            // Lancer le parsing
            parser.parseProgram();
            
            System.out.println("\n=== Parsing Successfully! ===");
            
        } catch (ParseException e) {
            System.err.println("Parsing Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lit les tokens depuis un fichier au format output.txt
     */
    private static List<Token> readTokensFromFile(String filename) throws Exception {
        List<Token> tokens = new java.util.ArrayList<>();
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
                    String lexicalUnit = parts[1].trim();
                    Token token = convertLexicalUnitToToken(lexicalUnit);
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
        
        // Ajouter EOF à la fin
        tokens.add(Token.EOF);
        tokenRawLines.add("EOF");
        tokenLineNumbers.add(-1);
        
        return tokens;
    }

    /**
     * Convertit une unité lexicale (string) en Token enum
     */
    private static Token convertLexicalUnitToToken(String lexicalUnit) {
        switch (lexicalUnit.toUpperCase()) {
            case "PROG": return Token.PROG;
            case "PROGNAME": return Token.VAR_NAME; // PROGNAME est traité comme VAR_NAME
            case "IS": return Token.IS;
            case "END": return Token.END;
            case "SEMI": return Token.SEMICOLON;
            case "ASSIGN": return Token.ASSIGN;
            case "PLUS": return Token.PLUS;
            case "MINUS": return Token.MINUS;
            case "MULT": return Token.MULT;
            case "DIV": return Token.DIV;
            case "VARNAME": return Token.VAR_NAME;
            case "NUMBER": return Token.NUMBER;
            case "LPAREN": return Token.OPEN_PAREN;
            case "RPAREN": return Token.CLOSE_PAREN;
            case "ARROW": return Token.ARROW;
            case "EQUAL": return Token.EQUAL_EQUAL;
            case "SMALEQ": return Token.LESS_EQUAL;
            case "SMALLER": return Token.LESS;
            case "WHILE": return Token.WHILE;
            case "DO": return Token.DO;
            case "IF": return Token.IF;
            case "PRINT": return Token.PRINT;
            case "INPUT": return Token.INPUT;
            case "LBRACK": return Token.OPEN_CURLY;
            case "RBRACK": return Token.CLOSE_CURLY;
            case "THEN": return Token.THEN;
            case "ELSE": return Token.ELSE;
            case "PIPE": return Token.PIPE;
            default:
                System.err.println("ERROR: Unknown Symbol: " + lexicalUnit);
                return null;
        }
    }
}

