
%%

%class LexicalAnalyzer
%unicode
%line
%column
%standalone

%{

%}

ShortComment = \$(.)*
LongComment = \!\![^]*\!\!

ProgName = [A-Z][A-Za-z_]*
VarName = [a-z][a-z0-9]*
Number = [0-9][0-9]*

%%
{Number}        { System.out.println("Number : " + yytext()); }
{VarName}       { System.out.println("Variable Name : " + yytext()); }
{ProgName}      { System.out.println("Program Name : " + yytext()); }
{ShortComment}  { System.out.println("Short Comment : " + yytext()); }
{LongComment}   { System.out.println("Long Comment : " + yytext()); }
.               {}

