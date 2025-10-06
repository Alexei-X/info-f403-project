import java.util.*;
import java.io.Reader;


%%

%class LexicalAnalyzer
%unicode
%line
%column
%standalone

%{
/* Fonction d'affichage */
private void tok(String kind) {
    System.out.println(kind + " : " + yytext());
}
private void err(String message) {
    System.out.println("LEX ERROR : " + yytext());
}
%}

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


/* Faudrait faire un état pour les longs commentaires */

%%

/*Comments*/
{ShortComment}  { tok("Short Comment"); }
{LongComment}   { tok("Long Comment"); }

/*Keywords*/
{Prog}          { tok("Prog");}
{Is}            { tok("Is");}
{End}           { tok("End");}
"Assign"        { tok("Assign");}
{If}            { tok("If");}       /*ça normalement c'est une instruction, pas un token*/
{Then}          { tok("Then");}
{Else}          { tok("Else");}
{While}         { tok("While");}
{Do}            { tok("Do");}
{Print}         { tok("Print");}
{Input}         { tok("Input");}

/*Operators & ponctuation*/
"->"                   { tok("ARROW"); }
"=="                   { tok("EQEQ"); }
"<="                   { tok("LE");  }

"="                    { tok("EQ"); }
"<"                    { tok("LT"); }
"+"                    { tok("PLUS"); }
"-"                    { tok("MINUS"); }
"*"                    { tok("TIMES"); }
"/"                    { tok("DIV"); }
"|"                    { tok("OR");  }

"("                    { tok("LPAR"); }
")"                    { tok("RPAR"); }
"["                    { tok("LBRACKET"); }
"]"                    { tok("RBRACKET"); }
"{"                    { tok("LBRACE"); }
"}"                    { tok("RBRACE"); }
";"                    { tok("SEMICOLON"); }

/*Identifiers & numbers*/
{Number}        { tok("Number"); }
{VarName}       { tok("Variable"); }
{ProgName}      { tok("Program"); }


.               {   /* Va falloir implémenter la gestion d'erreurs*/    }