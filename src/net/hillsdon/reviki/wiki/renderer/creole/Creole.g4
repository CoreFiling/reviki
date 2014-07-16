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

heading   : HSt inline HEnd? ;

paragraph : inline ;

ulist      : (ulist1 LineBreak?)+;
ulist1     : U1  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list2* ;
ulist2     : U2  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list3* ;
ulist3     : U3  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list4* ;
ulist4     : U4  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list5* ;
ulist5     : U5  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list6* ;
ulist6     : U6  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list7* ;
ulist7     : U7  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list8* ;
ulist8     : U8  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list9* ;
ulist9     : U9  (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list10* ;
ulist10    : U10 (WS ulist | olist | {nobreaks=true;} inline {nobreaks=false;}) ;

olist      : (olist1 LineBreak?)+ ;
olist1     : O1  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list2* ;
olist2     : O2  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list3* ;
olist3     : O3  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list4* ;
olist4     : O4  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list5* ;
olist5     : O5  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list6* ;
olist6     : O6  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list7* ;
olist7     : O7  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list8* ;
olist8     : O8  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list9* ;
olist9     : O9  (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) LineBreak? list10* ;
olist10    : O10 (WS olist | ulist | {nobreaks=true;} inline {nobreaks=false;}) ;

list2      : (olist2 | ulist2) LineBreak? ;
list3      : (olist3 | ulist3) LineBreak? ;
list4      : (olist4 | ulist4) LineBreak? ;
list5      : (olist5 | ulist5) LineBreak? ;
list6      : (olist6 | ulist6) LineBreak? ;
list7      : (olist7 | ulist7) LineBreak? ;
list8      : (olist8 | ulist8) LineBreak? ;
list9      : (olist9 | ulist9) LineBreak? ;
list10     : (olist10 | ulist10) LineBreak? ;

hrule      : Rule ;

table      : {nobreaks=true;} trow+ {nobreaks=false;};
trow       : tcell+ (RowEnd | LineBreak) ;
tcell      : th | td ;
th         : ThStart inline? ;
td         : TdStart inline? ;

nowiki     : NoWiki EndNoWikiBlock ;

/* ***** Inline Elements ***** */

inline     : inlinestep+ ;

inlinestep : bold | italic | sthrough
           | link | titlelink | simpleimg | imglink | wikiwlink | attachment | rawlink
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

attachment : Attachment ;

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