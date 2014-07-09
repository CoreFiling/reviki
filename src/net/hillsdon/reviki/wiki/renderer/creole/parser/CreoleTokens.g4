/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

/* ***** Headings ***** */

HSt  : LINE '='+ WS? {getText().trim().length() <= 6}? {setText(getText().trim());} ;
HEnd : ' '* '='+? ;

/* ***** Lists ***** */

U1 : LINE '@';
U2 : LINE '@@' ;
U3 : LINE '@@@' ;
U4 : LINE '@@@@' ;
U5 : LINE '@@@@@' ;

O1 : LINE '#' ;
O2 : LINE '##' ;
O3 : LINE '###' ;
O4 : LINE '####' ;
O5 : LINE '#####' ;

/* ***** Horizontal Rules ***** */

Rule : LINE '---' '-'+?;

/* ***** Tables ***** */

CellSep : '|' ;
TdStart : '|' ;
ThStart : '|=' ;

/* ***** Inline Formatting ***** */

Bold     : '**' ;
Italic   : '//' ;
Sthrough : '--' ;
NoWiki   : '{{{' -> mode(PREFORMATTED_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> mode(LINK) ;
ImSt  : '{{' -> mode(LINK) ;

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak LineBreak+ ;

LineBreak : '\r'? '\n'+? ;

/* ***** Links ***** */

RawUrl    : ALNUM+ ':' ~('['|']'|'"'|'\''|'('|')')+ ~(' '|'['|']'|'"'|'\''|'('|')'|','|'.')+?;

WikiWords : (ALNUM+ ':')? (UPPER ((ALNUM|'.')* ALNUM)*) (UPPER ((ALNUM|'.')* ALNUM)*)+;

/* ***** Miscellaneous ***** */

Any : . ;
WS  : (' '|'\t'|'\r'|'\n')+ -> skip ;

fragment LINE  : ({getCharPositionInLine()==0}? WS? | LineBreak WS?);
fragment ALNUM : (ALPHA | DIGIT) ;
fragment ALPHA : (UPPER | LOWER) ;
fragment UPPER : ('A'..'Z') ;
fragment LOWER : ('a'..'z') ;
fragment DIGIT : ('0'..'9') ;

/* ***** Contextual stuff ***** */


mode LINK;

LiEnd : ']]' -> mode(DEFAULT_MODE) ;
ImEnd : '}}' -> mode(DEFAULT_MODE) ;

Sep : '|' ;

InLink : ~(']'|'}'|'|')+ ;

mode PREFORMATTED_INLINE;

AnyInlineText : ~('\r'|'\n') -> more;

AnyBlockText : AnyInlineText '\n' -> mode(PREFORMATTED_BLOCK), more ;

EndNoWikiInline : '}}}' (~'}' {_input.seek(_input.index()-1);} | EOF) -> mode(DEFAULT_MODE) ;

mode PREFORMATTED_BLOCK;

AnyText   : (' }}}' | . ) -> more ;

EndNoWikiBlock : '}}}' -> mode(DEFAULT_MODE) ;
