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
  public int listLevel = 0;
  boolean nowiki = false;
  boolean cpp = false;
  boolean html = false;
  boolean java = false;
  boolean xhtml = false;
  boolean xml = false;
  boolean intr = false;

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

  public void doList(int level) {
    listLevel = level;

    seek(-1);
    setStart();
    resetFormatting();
  }

  public void doUrl() {
    String url = getText();
    String last = url.substring(url.length()-1);
    String next = next();

    if(url.endsWith("://") || url.endsWith("mailto:")) { setType(Any); }

    String badEnds = inHeader ? "[\\.,)\"';:\\\\=-]" : "[\\.,)\"';:\\\\-]";

    while((last + next).equals("//") || last.matches(badEnds)) {
      seek(-1);
      url = url.substring(0, url.length() - 1);
      last = url.substring(url.length()-1);
      next = next();
    }
    setText(url);
  }

  public void breakOut() {
    resetFormatting();
    listLevel = 0;
    inHeader = false;
    intr = false;
    nowiki = false;
    cpp = false;
    html = false;
    java = false;
    xhtml = false;
    xml = false;
  }

  public String[] thisKillsTheFormatting() {
    String[] ends = new String[7];

    if(inHeader || intr) {
      ends[0] = "\n";
      ends[1] = "\r\n";
    } else {
      ends[0] = null;
      ends[1] = null;
    }

    if(intr) {
      ends[2] = "|";
    } else {
      ends[2] = null;
    }

    ends[3] = "\n\n";
    ends[4] = "\r\n\r\n";

    if(listLevel > 0) {
      // \L (when at the start) matches the start of a line.
      ends[5] = "\\L*";
      ends[6] = "\\L#";
    } else {
      ends[5] = null;
      ends[6] = null;
    }

    return ends;
  }

  public void doInLink() {
    String txt = getText().trim();
    int len = txt.length();

    if(len > 2) {
      String lastButTwo = txt.substring(len - 3, len - 2);
      if(lastButTwo.equals("]") || lastButTwo.equals("}")) {
        txt = txt.substring(0, len - 2);
        seek(-2);
      }
    }

    setText(txt);
  }
}

/* ***** Headings ***** */

HSt  : LINE '='+ ~'=' {doHdr();} ;
HEnd : WS? '='* WS? (LineBreak | ParBreak) {inHeader}? {breakOut();} ;

/* ***** Lists ***** */

U1  : START '*' ~'*'                            {doList(1);} ;
U2  : START '**' ~'*'         {listLevel >= 1 && occurrencesBefore("**", "\n") % 2 == 0}? {doList(2);} ;
U3  : START '***' ~'*'        {listLevel >= 2}? {doList(3);} ;
U4  : START '****' ~'*'       {listLevel >= 3}? {doList(4);} ;
U5  : START '*****' ~'*'      {listLevel >= 4}? {doList(5);} ;
U6  : START '******' ~'*'     {listLevel >= 5}? {doList(6);} ;
U7  : START '*******' ~'*'    {listLevel >= 6}? {doList(7);} ;
U8  : START '********' ~'*'   {listLevel >= 7}? {doList(8);} ;
U9  : START '*********' ~'*'  {listLevel >= 8}? {doList(9);} ;
U10 : START '**********' ~'*' {listLevel >= 9}? {doList(10);} ;

O1  : START '#' ~'#'                            {doList(1);} ;
O2  : START '##' ~'#'         {listLevel >= 1}? {doList(2);} ;
O3  : START '###' ~'#'        {listLevel >= 2}? {doList(3);} ;
O4  : START '####' ~'#'       {listLevel >= 3}? {doList(4);} ;
O5  : START '#####' ~'#'      {listLevel >= 4}? {doList(5);} ;
O6  : START '######' ~'#'     {listLevel >= 5}? {doList(6);} ;
O7  : START '#######' ~'#'    {listLevel >= 6}? {doList(7);} ;
O8  : START '########' ~'#'   {listLevel >= 7}? {doList(8);} ;
O9  : START '#########' ~'#'  {listLevel >= 8}? {doList(9);} ;
O10 : START '##########' ~'#' {listLevel >= 9}? {doList(10);} ;

/* ***** Horizontal Rules ***** */

Rule : LINE '---' '-'+ {breakOut();} ;

/* ***** Tables ***** */

TdStartLn : LINE '|'+  {intr=true; setType(TdStart);} ;
ThStartLn : LINE '|'+ '=' {intr=true; setType(ThStart);} ;

RowEnd  : '|' WS? LineBreak {intr}? {breakOut();} ;
TdStart : '|'+  {intr}? {breakOut(); intr=true;} ;
ThStart : '|'+ '=' {intr}? {breakOut(); intr=true;} ;

/* ***** Inline Formatting ***** */

BSt : '**' {!bold.active}?   {setFormatting(bold,   Any);} ;
ISt : '//' {!italic.active && !prior().matches("[a-zA-Z0-9]:")}? {setFormatting(italic, Any);} ;
SSt : '--' {!strike.active}? {setFormatting(strike, Any);} ;

BEnd : '**' {bold.active}?   {unsetFormatting(bold);} ;
IEnd : '//' {italic.active && !prior().matches("[a-zA-Z0-9]:")}? {unsetFormatting(italic);} ;
SEnd : '--' {strike.active}? {unsetFormatting(strike);} ;

NoWiki     : '{{{'      {nowiki=true;} -> mode(CODE_INLINE) ;
StartCpp   : '[<c++>]'   {cpp=true;}   -> mode(CODE_INLINE) ;
StartHtml  : '[<html>]'  {html=true;}  -> mode(CODE_INLINE) ;
StartJava  : '[<java>]'  {java=true;}  -> mode(CODE_INLINE) ;
StartXhtml : '[<xhtml>]' {xhtml=true;} -> mode(CODE_INLINE) ;
StartXml   : '[<xml>]'   {xml=true;}   -> mode(CODE_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> mode(LINK) ;
ImSt  : '{{' -> mode(LINK) ;

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak WS? LineBreak+ {breakOut();} ;

LineBreak : '\r'? '\n' ;

/* ***** Links ***** */

RawUrl : PROTOCOL (~(' '|'\t'|'\r'|'\n'|'|'|'['|']')+ '/'?)+ {doUrl();} ;

fragment PROTOCOL : ('http' 's'? | 'file' | 'ftp') '://' | 'file:/' | 'mailto:' ;

Attachment : ALNUM+ '.' ALNUM+ ;

WikiWords : (UPPER (ABBR | CAMEL) | INTERWIKI ALNUM+) {checkBounds("[\\.\\w:]", "\\w")}? ;

fragment INTERWIKI : ALPHA ALNUM+ ':' ;
fragment ABBR      : UPPER UPPER+ ;
fragment CAMEL     : (LOWNUM* UPNUM ALNUM* LOWER ALNUM* | ALNUM* LOWER ALNUM* UPNUM+) ;

/* ***** Macros ***** */

MacroSt : '<<' -> mode(MACRO) ;

/* ***** Miscellaneous ***** */

Any : . ;
WS  : (' '|'\t')+ ;

EmptyLink : ('[[' WS? ']]' | '{{' WS? '}}' |'[[' WS? '|' WS? ']]' | '{{' WS? '|' WS? '}}') -> skip ;

fragment NOTALNUM : ~('A'..'Z'|'a'..'z'|'0'..'9') ;
fragment START : {start}? | LINE ;
fragment LINE  : {getCharPositionInLine()==0}? (' '|'\t')*;
fragment LOWNUM : (LOWER | DIGIT) ;
fragment UPNUM  : (UPPER | DIGIT) ;
fragment ALNUM : (ALPHA | DIGIT) ;
fragment ALPHA : (UPPER | LOWER) ;
fragment UPPER : ('A'..'Z') ;
fragment LOWER : ('a'..'z') ;
fragment DIGIT : ('0'..'9') ;

/* ***** Contextual stuff ***** */

mode LINK;

LiEnd : (']]' | '\r'? '\n') -> mode(DEFAULT_MODE) ;
ImEnd : ('}}' | '\r'? '\n') -> mode(DEFAULT_MODE) ;

Sep : ' '* '|' ' '*;

InLink : (~('|'|'\r'|'\n'|']'|'}') | (']' ~']' | '}' ~'}'))+ {doInLink();};

mode MACRO;

MacroName : ~(':'|'>')+ ;

MacroEndNoArgs : '>>' -> mode(DEFAULT_MODE) ;

MacroSep  : ':' -> mode(MACRO_ARGS) ;

mode MACRO_ARGS;

MacroArgs : . -> more ;

MacroEnd  : '>>' -> mode(DEFAULT_MODE) ;

mode CODE_INLINE;

AnyInline : ~('\r'|'\n') -> more;

OopsItsABlock : ('\r'|'\n') {seek(-1);} -> mode(CODE_BLOCK), more ;

EndNoWikiInline : '}}}' ~'}' {nowiki}? {nowiki=false; seek(-1);} -> mode(DEFAULT_MODE) ;
EndCppInline   : '[</c++>]'   {cpp}?   {cpp=false;}   -> mode(DEFAULT_MODE) ;
EndHtmlInline  : '[</html>]'  {html}?  {html=false;}  -> mode(DEFAULT_MODE) ;
EndJavaInline  : '[</java>]'  {java}?  {java=false;}  -> mode(DEFAULT_MODE) ;
EndXhtmlInline : '[</xhtml>]' {xhtml}? {xhtml=false;} -> mode(DEFAULT_MODE) ;
EndXmlInline   : '[</xml>]'   {xml}?   {xml=false;}   -> mode(DEFAULT_MODE) ;

mode CODE_BLOCK;

AnyText   : . -> more ;

EndNoWikiBlock : (~' ' '}}}' | ' }}}' '\r'? '\n' {seek(-1);}) {nowiki}? {nowiki=false;} -> mode(DEFAULT_MODE) ;
EndCppBlock    : ~' ' '[</cpp>]'   {cpp}?   {cpp=false;}   -> mode(DEFAULT_MODE) ;
EndHtmlBlock   : ~' ' '[</html>]'  {html}?  {html=false;}  -> mode(DEFAULT_MODE) ;
EndJavaBlock   : ~' ' '[</java>]'  {java}?  {java=false;}  -> mode(DEFAULT_MODE) ;
EndXhtmlBlock  : ~' ' '[</xhtml>]' {xhtml}? {xhtml=false;} -> mode(DEFAULT_MODE) ;
EndXmlBlock    : ~' ' '[</xml>]'   {xml}?   {xml=false;}   -> mode(DEFAULT_MODE) ;
