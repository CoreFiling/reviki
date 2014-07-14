/* Todo:
 *  - Comments justifying and explaining every rule.
 *  - Allow arbitrarily-nested lists (see actions/attributes)
 */

parser grammar Creole;

options { tokenVocab=CreoleTokens; }

@members {
  public boolean nobreaks = false;
}

/* ***** Top level elements ***** */

creole    : (block (LineBreak | ParBreak)*)* EOF ;

block     : heading
          | ulist | olist
          | hrule
          | table
          | code | nowiki
          | paragraph
          ;

/* ***** Block Elements ***** */

heading   : HSt inline HEnd ;

paragraph : inline ;

ulist      : ulist1+ ;
ulist1     : U1 (WS ulist | olist | inline) ulist2* ;
ulist2     : U2 (WS ulist | olist | inline) ulist3* ;
ulist3     : U3 (WS ulist | olist | inline) ulist4* ;
ulist4     : U4 (WS ulist | olist | inline) ulist5* ;
ulist5     : U5 (WS ulist | olist | inline) ;

olist      : olist1+ ;
olist1     : O1 (WS olist | ulist | inline) olist2* ;
olist2     : O2 (WS olist | ulist | inline) olist3* ;
olist3     : O3 (WS olist | ulist | inline) olist4* ;
olist4     : O4 (WS olist | ulist | inline) olist5* ;
olist5     : O5 (WS olist | ulist | inline) ;

hrule      : Rule ;

table      : {nobreaks=true;} (trow LineBreak)* trow (LineBreak | EOF) {nobreaks=false;};
trow       : tcell+ CellSep?;
tcell      : th | td ;
th         : ThStart inline? ;
td         : CellSep inline? ;

nowiki     : NoWiki EndNoWikiBlock ;

/* ***** Inline Elements ***** */

inline     : inlinestep+ ;

inlinestep : bold | italic | sthrough
           | link | titlelink | simpleimg | imglink | wikiwlink | rawlink
           | inlinecode | preformat
           | linebreak
           | macro
           | any
           ;

bold       : BSt inline? BEnd ;

italic     : ISt inline? IEnd ;

sthrough   : SSt inline? SEnd ;

link       : LiSt InLink LiEnd ;

titlelink  : LiSt InLink Sep InLink LiEnd ;

imglink    : ImSt InLink Sep InLink ImEnd ;

simpleimg  : ImSt InLink ImEnd ;

wikiwlink  : WikiWords ;

rawlink    : RawUrl ;

preformat  : NoWiki EndNoWikiInline ;

linebreak  : InlineBrk ({!nobreaks}? LineBreak)? ;

macro      : MacroSt MacroName MacroSep MacroEnd ;

any        : Any | WS | {!nobreaks}? LineBreak ;

/* ***** Syntax Highlighting ***** */

code        : cpp       | html       | java       | xhtml       | xml ;
inlinecode  : inlinecpp | inlinehtml | inlinejava | inlinexhtml | inlinexml ;

cpp         : StartCpp EndCppBlock ;
inlinecpp   : StartCpp EndCppInline ;

html        : StartHtml EndHtmlBlock ;
inlinehtml  : StartHtml EndHtmlInline ;

java        : StartJava EndJavaBlock ;
inlinejava  : StartJava EndJavaInline ;

xhtml       : StartXhtml EndXhtmlBlock ;
inlinexhtml : StartXhtml EndXhtmlInline ;

xml         : StartXml EndXmlBlock ;
inlinexml   : StartXml EndXmlInline ;