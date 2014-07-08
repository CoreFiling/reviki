/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

/* ***** Headings ***** */

H1   : '=' ;
H2   : '==' ;
H3   : '===' ;
H4   : '====' ;
H5   : '=====' ;
H6   : '======' ;
HEnd : ' '* '='+? ;

/* ***** Lists ***** */

U1 : '@';
U2 : '@@' ;
U3 : '@@@' ;
U4 : '@@@@' ;
U5 : '@@@@@' ;

O1 : '#' ;
O2 : '##' ;
O3 : '###' ;
O4 : '####' ;
O5 : '#####' ;

/* ***** Horizontal Rules ***** */

Rule : '---' '-'+? ;

/* ***** Tables ***** */

CellSep : '|' ;
TdStart : '|' ;
ThStart : '|=' ;

/* ***** Inline Formatting ***** */

Bold         : '**' ;
Italic       : '//' ;
Sthrough     : '--' ;
NoWikiInline : '{{{' -> pushMode(PREFORMATTED_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> pushMode(LINK);
ImSt  : '{{' -> pushMode(LINK);

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak LineBreak+ ;

LineBreak : '\r'? '\n'+? ;

/* ***** Links ***** */

RawUrl    : ALNUM+ ':' ~('['|']'|'"'|'\''|'('|')')+ ~(' '|'['|']'|'"'|'\''|'('|')'|','|'.')+?;

WikiWords : (ALNUM+ ':')? (UPPER ((ALNUM|'.')* ALNUM)*) (UPPER ((ALNUM|'.')* ALNUM)*)+;

/* ***** Plain Text ***** */

NoWiki    : '{{{' -> pushMode(PREFORMATTED);

/* ***** Miscellaneous ***** */

Any : . ;
WS  : (' '|'\t'|'\r'|'\n')+ ;

fragment ALNUM : (ALPHA | DIGIT) ;
fragment ALPHA : (UPPER | LOWER) ;
fragment UPPER : ('A'..'Z') ;
fragment LOWER : ('a'..'z') ;
fragment DIGIT : ('0'..'9') ;

/* ***** Contextual stuff ***** */


mode LINK;

LiEnd : ']]' -> popMode ;
ImEnd : '}}' -> popMode ;

Sep : '|' ;

InLink : ~(']'|'}'|'|')+ ;

mode PREFORMATTED_INLINE;

AnyInlineText : (~('\r'|'\n'|'}')* '}'? ~('\r'|'\n'|'}'))*;

EndNoWikiInline : '}}}' -> popMode ;

mode PREFORMATTED;

AnyText   : ' }}}'
          | .
          ; 

EndNoWiki : '}}}' -> popMode ;