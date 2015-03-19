lexer grammar CreoleTokens;

options { superClass=ContextSensitiveLexer; }

@members {
  // We keep track of whether we're in bold or not with this state. It's not
  // ideal, but as the start/end formatting tokens are all the same, this is
  // probably the best we can do (short of going crazy with lexer modes).
  Formatting bold;
  Formatting italic;
  Formatting strike;

  public java.util.List<Formatting> setupFormatting() {
    bold   = new Formatting("**", BSt, BEnd);
    italic = new Formatting("//", ISt, IEnd);
    strike = new Formatting("--", SSt, SEnd);

    return com.google.common.collect.ImmutableList.of(bold, italic, strike);
  }

  public boolean jiraStyleLinks = false;

  // track when we are in a terseblock quote so the we match start and
  // end tokens correctly.
  public boolean inTerseBlockquote = false;

  public String codeType;

  public CreoleTokens(CharStream input, boolean jiraStyleLinks) {
    this(input);
    this.jiraStyleLinks = jiraStyleLinks;
  }

  public boolean inHeader = false;
  public boolean start = false;
  public int listLevel = 0;
  boolean intr = false;

  // Start matching a list item: this updates the list level (allowing deeper
  // list tokens to be matched), and breaks out of any formatting we may have
  // going on - which may trigger parser error-correction.
  public void doList(int level) {
    seek(-1);

    listLevel = level;

    resetFormatting();

    String next1 = get(0);
    String next2 = get(1);
    start = (next1.equals("*") && !next2.equals("*")) || (next1.equals("#") && !next2.equals("#"));
  }

  // When we think we've matched a URL, seek back through it until we have
  // something more reasonable looking.
  public void doUrl() {
    String url = getText();
    String last = url.substring(url.length()-1);
    String next = next(1);

    String badEnds = inHeader ? "[\\.,)\"';:\\\\=-]" : "[\\.,)\"';:\\\\-]";

    while((last + next).equals("//") || last.matches(badEnds)) {
      seek(-1);
      url = url.substring(0, url.length() - 1);
      last = url.substring(url.length()-1);
      next = next(1);

      // Break out if we no longer have a URL
      if(url.endsWith(":/") || url.endsWith("mailto:")) {
        setType(Any);
        break;
      }
    }
    setText(url);
  }

  // Reset all special lexer state.
  public void breakOut() {
    resetFormatting();
    listLevel = 0;
    inHeader = false;
    intr = false;
  }

  public void doCodeTagStart(int toMode) {
    String tag = getText();
    String start = "[<";
    String end = ">]";
    codeType = tag.substring(start.length(), tag.length() - end.length());
    setText(codeType);
    mode(toMode);
  }
  
  public void doEndCodeTag() {
    String txt = getText();
    String start = "[</";
    String end = ">]";
    if (txt.endsWith(codeType + end)) {
      seek(0 - (start.length() + end.length() + codeType.length()));
      mode(CODETAG_END);
    } else {
      more();
    }
  }

  // Determine which tokens can, at this stage, break out of any inline
  // formatting.
  public java.util.List<String> thisKillsTheFormatting() {
    java.util.List<String> ends = new java.util.ArrayList<String>();

    if(inHeader || intr) {
      ends.add("\n");
      ends.add("\r\n");
    }
    else {
      ends.add("\n\n");
      ends.add("\r\n\r\n");
    }

    if(intr) {
      ends.add("|");
    }

    if(listLevel > 0) {
      // \L (when at the start) matches the start of a line.
      ends.add("\\L*");
      ends.add("\\L#");
    }

    return ends;
  }

  // Handle a link target or title ending with a ']' or '}'.
  public void doLinkEnd() {
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

HSt  : LINE ('=' | '==' | '===' | '====' | '=====' | '======') ~'=' {inHeader=true; seek(-1);} ;
HEnd : WS? '='* WS? (LineBreak | ParBreak) {inHeader}? {breakOut();} ;

/* ***** Lists ***** */

// Lists are messy, as in the parser. One notable trait is that sublists can
// start with any combination of *s and #s, which is perhaps too flexible, but
// it works (and was easy to specify). It means we can do things like this:
// * Level 1 Unordered
// *# Sublist which is ordered
// *#* Sublist which is unordered
// #** Item of same unordered sublist
// ##* Item of same unordered sublist

U1  : START U                            {doList(1);} ;
U2  : START L U         {listLevel >= 1}? {doList(2);} ;
U3  : START L L U        {listLevel >= 2}? {doList(3);} ;
U4  : START L L L U       {listLevel >= 3}? {doList(4);} ;
U5  : START L L L L U      {listLevel >= 4}? {doList(5);} ;
U6  : START L L L L L U     {listLevel >= 5}? {doList(6);} ;
U7  : START L L L L L L U    {listLevel >= 6}? {doList(7);} ;
U8  : START L L L L L L L U   {listLevel >= 7}? {doList(8);} ;
U9  : START L L L L L L L L U  {listLevel >= 8}? {doList(9);} ;
U10 : START L L L L L L L L L U {listLevel >= 9}? {doList(10);} ;

O1  : START O                            {doList(1);} ;
O2  : START L O         {listLevel >= 1}? {doList(2);} ;
O3  : START L L O        {listLevel >= 2}? {doList(3);} ;
O4  : START L L L O       {listLevel >= 3}? {doList(4);} ;
O5  : START L L L L O      {listLevel >= 4}? {doList(5);} ;
O6  : START L L L L L O     {listLevel >= 5}? {doList(6);} ;
O7  : START L L L L L L O    {listLevel >= 6}? {doList(7);} ;
O8  : START L L L L L L L O   {listLevel >= 7}? {doList(8);} ;
O9  : START L L L L L L L L O  {listLevel >= 8}? {doList(9);} ;
O10 : START L L L L L L L L L O {listLevel >= 9}? {doList(10);} ;

fragment U : '*' ~('*'|'\r'|'\n') ;
fragment O : '#' ~('#'|'\r'|'\n') ;
fragment L : '*' | '#' ;

/* ***** Horizontal Rules ***** */

Rule : LINE '---' '-'+ {breakOut();} ;

/* ***** Tables ***** */

TdStartLn : LINE '|'  {intr=true; setType(TdStart);} ;
ThStartLn : LINE '|' '=' {intr=true; setType(ThStart);} ;

RowEnd  : '|' WS? LineBreak {intr}? {breakOut();} ;
TdStart : '|'  {intr}? {breakOut(); intr=true;} ;
ThStart : '|' '=' {intr}? {breakOut(); intr=true;} ;

/* ***** Inline Formatting ***** */

Bold   : '**' {toggleFormatting(bold, Any);} ;
Italic : '//' {prior() == null || (prior() != ':' || !Character.isLetterOrDigit(priorprior()))}? {toggleFormatting(italic, Any);} ;
Strike : '--' {toggleFormatting(strike, Any);} ;

NoWiki     : '{{{'       -> mode(NOWIKI_INLINE) ;

fragment CODETAGTYPE : 'c++' | 'java' | 'xhtml' | 'xml' ;
fragment CODETAGTYPEHTML : CODETAGTYPE | 'html';

CodeTagStart  : '[<' CODETAGTYPE '>]' {doCodeTagStart(CODETAG_INLINE);} ;
HtmlStart  : '[<html>]' {doCodeTagStart(HTML_INLINE);} ;

/* ***** Code formatting ***** */

CodeStart : START '```' .*? LineBreak {setText(getText().trim().substring(3));} -> mode(CODE_BLOCK) ;
CodeInlineStart : '`' -> mode(CODE_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> mode(LINK) ;
ImSt  : '{{' -> mode(LINK) ;
AnSt  : '[[#' -> mode(ANCHOR) ;
JIRALiSt : {jiraStyleLinks}? '[' -> mode(JIRALINK) ;

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak WS? LineBreak+ {breakOut();} ;

LineBreak : '\r'? '\n' ;

/* ***** Links ***** */

RawUrl : PROTOCOL (~(' '|'\t'|'\r'|'\n'|'|'|'['|']'|'"'|'/')+ '/'?)+ {doUrl();} ;

fragment PROTOCOL : ('http' 's'? | 'file' | 'ftp') '://' | 'file:/' | 'mailto:' ;

Attachment : UPPER CAMEL '.' ALNUM+ ;

WikiWords : (UPPER (ABBR | CAMEL) REVISION? | INTERWIKI IWTARGET+) NOTALNUM {prior() == null || prior() != '.' && prior() != ':' && !Character.isLetterOrDigit(prior()) && !(last() == '.' && Character.isLetter(next()))}? {seek(-1);} ;

fragment IWTARGET  : ALNUM (('.' | '-') ALNUM)? ;
fragment INTERWIKI : ALPHA ALNUM+ ':' ;
fragment ABBR      : UPPER (UPNUM | ('-' UPNUM))+ ;
fragment CAMEL     : (LOWNUM* UPNUM ALNUM* LOWER ALNUM* | ALNUM* LOWER ALNUM* UPNUM+) ;
fragment REVISION  : '?revision=' DIGIT+ ;

/* ***** Macros ***** */

MacroSt : '<<' -> mode(MACRO) ;

DirectiveEnable  : '<<+' -> mode(MACRO) ;
DirectiveDisable : '<<-' -> mode(MACRO) ;

/* ***** Quotes ***** */

BlockquoteSt  : '[<blockquote>]' ;
BlockquoteEnd : '[</blockquote>]' ;

TerseBlockquoteSt  : {!inTerseBlockquote}? '"""' {inTerseBlockquote = true;};
TerseBlockquoteEnd : {inTerseBlockquote}? '"""' {inTerseBlockquote = false;};

/* ***** Miscellaneous ***** */

Any : ALNUM+ | . ;
WS  : (' '|'\t')+ ;

// Skip empty links like [[]] and {{}}. These are commonly used to break up WikiWords.
EmptyLink : ('[[' WS? ']]' | '{{' WS? '}}' |'[[' WS? '|' WS? ']]' | '{{' WS? '|' WS? '}}') -> skip ;

fragment NOTALNUM : ~('A'..'Z'|'a'..'z'|'0'..'9') ;
fragment LOWNUM : (LOWER | DIGIT) ;
fragment UPNUM  : (UPPER | DIGIT) ;
fragment ALNUM : (ALPHA | DIGIT) ;
fragment ALPHA : (UPPER | LOWER) ;
fragment UPPER : ('A'..'Z') ;
fragment LOWER : ('a'..'z') ;
fragment DIGIT : ('0'..'9') ;

// 'START' matches something which is start-of-line-like. Currently that's upon
// entering a list item or table cell
fragment START : {start}? | {intr && priorTokId == TdStart}? WS* | LINE ;
fragment LINE  : {getCharPositionInLine()==0}? (' '|'\t')*;

/* ***** Contextual stuff ***** */

mode LINK;

LiEnd : (']]' | '\r'? '\n') -> mode(DEFAULT_MODE) ;
ImEnd : ('}}' | '\r'? '\n') -> mode(DEFAULT_MODE) ;

Sep : ' '* '|'+ ' '* -> mode(LINK_END);

InLink : (~('|'|'\r'|'\n'|']'|'}') | (']' ~']' | '}' ~'}'))+ {doLinkEnd();} ;

mode JIRALINK;

JIRALiEnd : (']' | '\r'? '\n') -> mode(DEFAULT_MODE) ;

JIRASep : ' '* '|'+ ' '* -> mode(JIRALINK_END);

JIRAInLink : (~('|'|'\r'|'\n'|']'))+ ;

mode LINK_END;

InLinkEnd : (~('\r'|'\n'|']'|'}') | (']' ~']' | '}' ~'}'))+ {doLinkEnd();} ;

LiEnd2 : (']]' | '\r'? '\n') -> mode(DEFAULT_MODE) ;
ImEnd2 : ('}}' | '\r'? '\n') -> mode(DEFAULT_MODE) ;

mode JIRALINK_END;

JIRAInLinkEnd : (~('\r'|'\n'|']'))+ ;

JIRALiEnd2 : (']' | '\r'? '\n') -> mode(DEFAULT_MODE) ;

mode ANCHOR;

InAnchor : ~('#'|']')+ ;

AnEnd : ']]' -> mode(DEFAULT_MODE);

mode MACRO;

MacroName : ~(':'|'>')+ ;

MacroEndNoArgs : '>>' -> mode(DEFAULT_MODE) ;

MacroSep  : ':' -> mode(MACRO_ARGS) ;

mode MACRO_ARGS;

MacroArgs : . -> more ;

MacroEnd  : '>>' -> mode(DEFAULT_MODE) ;

// ***** NoWiki

mode NOWIKI_INLINE;

fragment INLINE  : ~('\r'|'\n') ;
fragment BLOCK   : . ;
fragment TOBLOCK : {_input.LA(1) == '\r' || _input.LA(1) == '\n'}? ;

NoWikiInline    : INLINE -> more ;
NoWikiToBlock   : TOBLOCK -> mode(NOWIKI_BLOCK), more ;
NoWikiInlineAny : INLINE*? '}}}' {seek(-3);} -> mode(NOWIKI_END);

mode NOWIKI_BLOCK;

NoWikiAny      : .*? '}}}' {seek(-3);} -> mode(NOWIKI_END);

mode NOWIKI_END;

EndNoWiki : '}}}' -> mode(DEFAULT_MODE) ;

// ***** CodeTag

mode CODETAG_INLINE;

fragment ENDCODETAG : '[</' CODETAGTYPE '>]' ;
fragment ENDCODETAGHTML : '[</' CODETAGTYPEHTML '>]' ;

CodeTagInline : INLINE -> more ;
CodeTagToBlock : TOBLOCK -> mode(CODETAG_BLOCK), more ;
CodeTagInlineAny : INLINE*? '[</' CODETAGTYPE '>]' {doEndCodeTag();} ;

mode CODETAG_BLOCK;

CodeTagAny    : .*? ENDCODETAG {doEndCodeTag();} ;

mode CODETAG_END;

CodeTagEnd : ENDCODETAGHTML -> mode(DEFAULT_MODE) ;

mode HTML_INLINE;

HtmlInline : INLINE -> more ;
HtmlToBlock : TOBLOCK -> mode(HTML_BLOCK), more ;
HtmlInlineAny : INLINE*? '[</html>]' {doEndCodeTag();} ;

mode HTML_BLOCK;

HtmlAny    : .*? '[</html>]' {doEndCodeTag();} ;

// ***** Code

mode CODE_BLOCK;

CodeAny : .*? '```' {seek(-3);} -> mode(CODE_BLOCK_END) ;

mode CODE_BLOCK_END;

CodeEnd : '```' -> mode(DEFAULT_MODE) ;

mode CODE_INLINE;

CodeInlineAny : .*? ('`' | LineBreak) {seek(-1);} -> mode(CODE_INLINE_END) ;

mode CODE_INLINE_END;

CodeInlineEnd : ('`' | LineBreak) -> mode(DEFAULT_MODE) ;

// Helper token types, not directly matched, but seta s the type of other tokens.
mode HELPERS;

BSt : ;
ISt : ;
SSt : ;
BEnd : ;
IEnd : ;
SEnd : ;
