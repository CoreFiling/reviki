/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

options { superClass=ContextSensitiveLexer; }

@members {
  Formatting bold;
  Formatting italic;
  Formatting strike;

  public void setupFormatting() {
    bold   = new Formatting("**");
    italic = new Formatting("//");
    strike = new Formatting("--");

    inlineFormatting.add(bold);
    inlineFormatting.add(italic);
    inlineFormatting.add(strike);
  }

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
        seek(-1);
      }

      setText(prefix);
      inHeader = true;
    } else {
      setType(Any);
    }
  }

  public void setStart() {
    String next1 = next();
    String next2 = get(1);
    start = (next1.equals("*") && !next2.equals("*")) || (next1.equals("#") && !next2.equals("#"));
  }

  public void doUlist(int level) {
    ulistLevel = level;

    seek(-1);
    setStart();
  }

  public void doOlist(int level) {
    olistLevel = level;

    seek(-1);
    setStart();
  }

  public void doUrl() {
    String url = getText();
    String last = url.substring(url.length()-1);
    String next = next();

    if((last + next).equals("//") || (last+next).equals(". ") || (last+next).equals(", ")) {
      seek(-1);
      setText(url.substring(0, url.length() - 1));
    }
  }
}

/* ***** Headings ***** */

HSt  : LINE '='+ ~'=' WS? {doHdr();} ;
HEnd : ' '* '='* ('\r'? '\n' {seek(-1);} | EOF) {inHeader}? {inHeader = false; resetFormatting();} ;

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

BSt : '**' {!bold.active}?   {setFormatting(bold,   Any);} ;
ISt : '//' {!italic.active}? {setFormatting(italic, Any);} ;
SSt : '--' {!strike.active}? {setFormatting(strike, Any);} ;

BEnd : '**' {bold.active}?   {unsetFormatting(bold);} ;
IEnd : '//' {italic.active}? {unsetFormatting(italic);} ;
SEnd : '--' {strike.active}? {unsetFormatting(strike);} ;

NoWiki : '{{{' -> mode(PREFORMATTED_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> mode(LINK) ;
ImSt  : '{{' -> mode(LINK) ;

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak LineBreak+ {resetFormatting();} ;

LineBreak : '\r'? '\n'+? ;

/* ***** Links ***** */

RawUrl    : ('http' | 'ftp') '://' (~(' '|'\t'|'\r'|'\n'|'/')+ '/'?)+ {doUrl();};

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

OopsItsABlock : ('\r'|'\n') -> mode(PREFORMATTED_BLOCK), more ;

EndNoWikiInline : '}}}' (~'}' {seek(-1);} | EOF) -> mode(DEFAULT_MODE) ;

mode PREFORMATTED_BLOCK;

AnyText   : . -> more ;

EndNoWikiBlock : LINE '}}}' -> mode(DEFAULT_MODE) ;
