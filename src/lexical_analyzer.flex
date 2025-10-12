import java.util.*;
import java.io.Reader;


%%

%class LexicalAnalyzer
%unicode
%line
%column
%standalone

%{
/**
* Symbol Table attribute
*/
private SymbolTable symbol_table = new SymbolTable();
private TokenSequence token_sequence = new TokenSequence();

/** 
* Adds a new token to the symbol Table
* @param kind the LexicalUnit detected by scanner
*/
private void tok(LexicalUnit kind) {
    symbol_table.addSymbol(kind, yyline, yycolumn, yytext());
    token_sequence.addSymbol(kind, yytext());
}
/**
* Prints error to output
* @param message message to be printed
*/
private void err() {
    System.out.println("LEX ERROR : " + yytext());
}
%}

%eof{
    token_sequence.printSequence();
    symbol_table.printTable();
%eof}

/* Space & End of line */
WS            = [ \t\f]+
NEWLINE       = \r\n|\r|\n

/*Comments*/
ShortComment = \$(.)*
LongComment   = "!!" [^]* "!!"

/*Keywords*/
Prog =      "Prog"
Is =        "Is"         
End =       "End"      
If =        "If"         
Then =      "Then"         
Else =      "Else"       
While =     "While"      
Do =        "Do" 
Print =     "Print" 
Input =     "Input"


/*Identifiers & numbers*/
ProgName = [A-Z][A-Za-z_]*
VarName = [a-z][a-z0-9]*
Number = [0-9][0-9]*


%%

/*Comments*/
{ShortComment}  {}
{LongComment}   {}

/*Keywords*/
{Prog}          { tok(LexicalUnit.PROG);}
{Is}            { tok(LexicalUnit.IS);}
{End}           { tok(LexicalUnit.END); }
"Assign"        { tok(LexicalUnit.ASSIGN);}
{If}            { tok(LexicalUnit.IF);}       /*ça normalement c'est une instruction, pas un token*/
{Then}          { tok(LexicalUnit.THEN);}
{Else}          { tok(LexicalUnit.ELSE);}
{While}         { tok(LexicalUnit.WHILE);}
{Do}            { tok(LexicalUnit.DO);}
{Print}         { tok(LexicalUnit.PRINT);}
{Input}         { tok(LexicalUnit.INPUT);}

/*Operators & ponctuation*/
"->"                   { tok(LexicalUnit.IMPLIES); }
"=="                   { tok(LexicalUnit.EQUAL); }
"<="                   { tok(LexicalUnit.SMALEQ);  }

"="                    { tok(LexicalUnit.ASSIGN); }
"<"                    { tok(LexicalUnit.SMALLER); }
"+"                    { tok(LexicalUnit.PLUS); }
"-"                    { tok(LexicalUnit.MINUS); }
"*"                    { tok(LexicalUnit.TIMES); }
"/"                    { tok(LexicalUnit.DIVIDE); }
"|"                    { tok(LexicalUnit.PIPE);  }

"("                    { tok(LexicalUnit.LPAREN); }
")"                    { tok(LexicalUnit.RPAREN); }
"{"                    { tok(LexicalUnit.LBRACK); }
"}"                    { tok(LexicalUnit.RBRACK); }
";"                    { tok(LexicalUnit.SEMI); }

/*Identifiers & numbers*/
{Number}        { tok(LexicalUnit.NUMBER); }
{VarName}       { tok(LexicalUnit.VARNAME); }
{ProgName}      { tok(LexicalUnit.PROGNAME); }
{WS}            { /* ignore for now */ }
{NEWLINE}       { /* ignore for now */ }


.               {   err()  ;  }
