lexer grammar ExpandingLexer;

EXPANSION: '$'IDENTIFIER;
DOLLAR: '$';
IDENTIFIER: [a-zA-Z_][0-9a-zA-Z_]*;
TEXT: ~('\n' | '$' | [a-zA-Z_])+;