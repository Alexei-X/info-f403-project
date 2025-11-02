import java.util.List;
import java.util.Arrays;

//Un parser a une liste de tokens et un index de la position actuelle
public class Parser{
    private List<String> tokens;
    private int currentTokenIndex;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
        this.currentTokenIndex = 0;
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
            throw new ParseException("Error: Expected " + expected + " got " + lookCurrent());
        } 
    }

    //  POINT D'ENTREE
    //  Production 1 : 
    //  <Program> -> Prog [ProgName] Is <Code> End
    public void parseProgram() throws ParseException {
        System.out.println("Parsing <Program>");

        match(token.PROG);
        match(Token.VAR_NAME);
        match(Token.IS);
        parseCode();
        match(Token.END);

        if (lookCurrent() != Token.EOF){
            throw new ParseException("Unexpected token after end of program");
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
                parseCode;
            }

        // Règle 3 : <Code> -> epsilon
        // Follow¹(<Code>) = {End, Else} [cite: 10]
        else if (nextToken == Token.END || nextToken == Token.ELSE){
            System.out.println("Epsilon for <Code>");
        }
        
        else{
            throw new ParseException("Error : Unexpected token in <Code> : " + nextToken);
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

    // Règle 9 : <Assign> -> [VarName] = <ExprArith>
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
            // Règle 11 :
            match(Token.PLUS);
            parseProd();
            parseExprArithPrime();
        }
        else if (nextToken == Token.MINUS) {
            //  Règle 12 :
            match(Token.MINUS);
            parseProd();
            parseExprArithPrime();
        }
        else if (   // l'enfer MDRRRRRRRRR
                    nextToken == Token.SEMICOLON || nextToken == Token.EQUAL_EQUAL || 
                    nextToken == Token.LESS_EQUAL || nextToken == Token.LESS ||
                    nextToken == Token.ARROW || nextToken == Token.PIPE ||
                    nextToken == Token.CLOSE_PAREN || nextToken == Token.CLOSE_CURLY){
                        //  Règle 13 :
                        System.out.println("Epsilon for <ExprArith'>"); // Tout ça pour ne rien faire 
                    }
        else {
            throw new ParseException("Error : Unexpected token in <ExprArith'> : " + nextToken);
        }
    }

    //  Règle 23
    public void parseIf() throws ParseException{
        System.out.println("Parsing <If>");

        match(Token.IF);
        match(Token.OPEN_CURLY); 
        parseCond();
        match(Token.CLOSE_CURLY); 
        match(Token.THEN)
        parseCode();
        parseIfPrime();
    }
    
    //  Règles 24 - 25 :
    //Pas encore bon
    /*
    public void parseIfPrime throws ParseException{
        System.out.println("parsing If'");
        Token nextToken == lookCurrent();

        if (nextToken == Token.END){
            match(Token.END);
        }
        else if (nextToken == Token.ELSE){
            match(Token.ELSE);
            parseCode();
            match(Token.END);
        }
        else {throw new ParseException("Error : Unexpected token in <IF> : " +  nextToken); }
    } */

    public void parseWhile() throws ParseException{

    }
    public void parseOutput() throws ParseException{

    }
    public void parseInput() throws ParseException{

    }



    public static void main(String[] args){
    }
}

