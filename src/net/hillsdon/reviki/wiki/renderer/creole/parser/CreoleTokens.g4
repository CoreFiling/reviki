/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

@members {
  public boolean inHeader = false;
  public boolean start = false;
  public boolean bold = false;
  public boolean italic = false;
  public boolean strike = false;
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

  public void resetFormatting() {
    bold   = false;
    italic = false;
    strike = false;
  }
}

/* ***** Headings ***** */

HSt  : LINE '='+ ~'=' {doHdr();} ;
HEnd : ' '* '='+ {inHeader}? {inHeader = false; resetFormatting();} ;

/* ***** Lists ***** */

U1 : START '*' ~'*'                        {doUlist(1); resetFormatting();} ;
U2 : START '**' ~'*'    {ulistLevel >= 1}? {doUlist(2); resetFormatting();} ;
U3 : START '***' ~'*'   {ulistLevel >= 2}? {doUlist(3); resetFormatting();} ;
U4 : START '****' ~'*'  {ulistLevel >= 3}? {doUlist(4); resetFormatting();} ;
U5 : START '*****' ~'*' {ulistLevel >= 4}? {doUlist(5); resetFormatting();} ;

O1 : START '#' ~'#'                        {doOlist(1); resetFormatting();} ;
O2 : START '##' ~'#'    {olistLevel >= 1}? {doOlist(2); resetFormatting();} ;
O3 : START '###' ~'#'   {olistLevel >= 2}? {doOlist(3); resetFormatting();} ;
O4 : START '####' ~'#'  {olistLevel >= 3}? {doOlist(4); resetFormatting();} ;
O5 : START '#####' ~'#' {olistLevel >= 4}? {doOlist(5); resetFormatting();} ;

/* ***** Horizontal Rules ***** */

Rule : LINE '---' '-'+? {resetFormatting();} ;

/* ***** Tables ***** */

CellSep : '|'  {resetFormatting();} ;
TdStart : '|'  {resetFormatting();} ;
ThStart : '|=' {resetFormatting();} ;

/* ***** Inline Formatting ***** */

BSt : '**' {!bold}?   {bold=true;} ;
ISt : '//' {!italic}? {italic=true;} ;
SSt : '--' {!strike}? {strike=true;} ;

BEnd : '**' {bold}?   {bold=false;} ;
IEnd : '//' {italic}? {italic=false;} ;
SEnd : '--' {strike}? {strike=false;} ;

NoWiki : '{{{' -> mode(PREFORMATTED_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> mode(LINK) ;
ImSt  : '{{' -> mode(LINK) ;

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak LineBreak+ {resetFormatting();} ;

LineBreak : '\r'? '\n'+? ;

/* ***** Links ***** */

RawUrl    : ('http' | 'ftp') '://' ~(' '|'\t'|'\r'|'\n')+;

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
