/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

@members {
  public boolean inHeader = false;
  public boolean start = false;
  public int olistLevel = 0;
  public int ulistLevel = 0;

  public void doHdr() {
    String prefix = getText().trim();
    boolean seekback = false;

    if(!prefix.substring(prefix.length() - 1).equals("=")) {
      prefix = prefix.substring(0, prefix.length() - 1);
      seekback = true;
    }

    if(prefix.length() <= 6) {
      if(seekback) {
        _input.seek(_input.index() - 1);
      }

      setText(prefix);
      inHeader = true;
    } else {
      setType(Any);
    }
  }

  public void setStart() {
    String next1 = _input.getText(new Interval(_input.index(), _input.index()));
    String next2 = _input.getText(new Interval(_input.index() + 1, _input.index() + 1));
    start = (next1.equals("*") && !next2.equals("*")) || (next1.equals("#") && !next2.equals("#"));
  }

  public void doUlist(int level) {
    ulistLevel = level;

    _input.seek(_input.index() - 1);
    setStart();
  }

  public void doOlist(int level) {
    olistLevel = level;

    _input.seek(_input.index() - 1);
    setStart();
  }
}

/* ***** Headings ***** */

HSt  : LINE '='+ ~'=' {doHdr();} ;
HEnd : ' '* '='+ {inHeader}? {inHeader = false;} ;

/* ***** Lists ***** */

U1 : START '*' ~'*'                        {doUlist(1);} ;
U2 : START '**' ~'*'    {ulistLevel >= 1}? {doUlist(2);} ;
U3 : START '***' ~'*'   {ulistLevel >= 2}? {doUlist(3);} ;
U4 : START '****' ~'*'  {ulistLevel >= 3}? {doUlist(4);} ;
U5 : START '*****' ~'*' {ulistLevel >= 4}? {doUlist(5);} ;

O1 : START '#' ~'#'                        {doOlist(1);} ;
O2 : START '##' ~'#'    {olistLevel >= 1}? {doOlist(2);} ;
O3 : START '###' ~'#'   {olistLevel >= 2}? {doOlist(3);} ;
O4 : START '####' ~'#'  {olistLevel >= 3}? {doOlist(4);} ;
O5 : START '#####' ~'#' {olistLevel >= 4}? {doOlist(5);} ;

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

fragment START : {start}? | LINE ;
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

AnyBlockText : AnyInlineText* LineBreak -> mode(PREFORMATTED_BLOCK), more ;

EndNoWikiInline : '}}}' (~'}' {_input.seek(_input.index()-1);} | EOF) -> mode(DEFAULT_MODE) ;

mode PREFORMATTED_BLOCK;

AnyText   : . -> more ;

EndNoWikiBlock : LINE '}}}' -> mode(DEFAULT_MODE) ;
