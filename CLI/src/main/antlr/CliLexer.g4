lexer grammar CliLexer;

IDENTIFIER: [a-zA-Z_][0-9a-zA-Z_]*;
EXPANSION: '$';
ASSIGN: '=';
PIPE: '|';
FQ: '\''~[\n']*'\'';
WQ: '"'~["\n]*'"';
WORD: ~(['" \n\t\r=|$] | [a-zA-Z_])+;
NL: '\n';
WS: [ \t\r]+;
ERROR : . ;