lexer grammar Lang24Lexer;

@header {
	package lang24.phase.lexan;
	import lang24.common.report.*;
	import lang24.data.token.*;
}

@members {
    @Override
	public LocLogToken nextToken() {
		return (LocLogToken) super.nextToken();
	}

	@Override
	public void notifyListeners(LexerNoViableAltException e) {
        var currentToken = new LocLogToken(_tokenFactorySourcePair, _type, _channel, _tokenStartCharIndex, _input.index());
		throw new Report.Error(currentToken, "token recognition error: " + currentToken.getText());
	}
}

fragment ESC_SEQ : '\\' [btnfr"'\\];
fragment VALIDCHAR : [ -~];
fragment HEXCHAR:  '\\' [A-F0-9][A-F0-9] ;

COMMENTS: '#' ~[\r\n]* '\r'? '\n' -> skip;

//Strings
STRING : '"' ( ESC_SEQ | HEXCHAR | ~('\\'|'"') )* '"';
CHARACTER : '\'' ( ESC_SEQ | HEXCHAR | ~('\\'|'\'') ) '\'';


CARET: '^';
NOT: 'not';
PLUS: '+';
MINUS: '-';



PREFIXOPERATOR
: NOT
| PLUS
| MINUS
| CARET
;

POSTFIXOPERATOR
: /*LBRACKET
| RBRACKET
|*/ CARET  /*
| DOT*/
;



COLON: ':';
COMMA: ',';
DOT: '.';
LESS: '<';
GREATER: '>';
SEMICOLON: ';';


//Keywords
NIL: 'nil';
NONE: 'none';
OR: 'or';
AND: 'and';
SIZEOF: 'sizeof';

//Data types
VOID: 'void';
BOOL: 'bool';
CHAR: 'char';
INT: 'int';

//Statements
IF: 'if';
ELSE: 'else';
THEN: 'then';
RETURN: 'return';
WHILE: 'while';

BOOLVAL: TRUE | FALSE;
fragment TRUE: 'true';
fragment FALSE: 'false';

//Values
IDENTIFIER: [A-Za-z_][A-Za-z0-9_]*;
NUMBER: DIGIT+;
fragment DIGIT: [0-9];



//Symbols



//Brackets
LBRACE: '{';
RBRACE: '}';
LPARENTHESIS: '(';
RPARENTHESIS: ')';
LBRACKET: '[';
RBRACKET: ']';

//Logical operators
EQUALS_R: '==';
NOTEQUALS_R: '!=';
LESS_OR_EQUALS_R: '<=';
GREATER_OR_EQUALS_R: '>=';

//Arethmetic operators
DIVIDE: '/';
STAR: '*';
PERCENT: '%';

EQUALS: '=';




//White Spaces
WS: [\n\t\r ]+ -> skip;