/* Todo:
 *  - Comments justifying and explaining every rule.
 *  - Allow arbitrarily-nested lists (see actions/attributes)
 */

parser grammar Creole;

options {
  tokenVocab=CreoleTokens;
  superClass=ContextSensitiveParser;
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

heading   : HSt WS? {noBreak();} inline {unsetBreaks();} HEnd;

paragraph : inline ;

ulist      : {yesBreak();} (ulist1 LineBreak?)* {unsetBreaks();} {noBreak();} ulist1 LineBreak? {unsetBreaks();};
ulist1     : U1  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list2* {unsetBreaks();} {noBreak();} list2 {unsetBreaks();})? ;
ulist2     : U2  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list3* {unsetBreaks();} {noBreak();} list3 {unsetBreaks();})? ;
ulist3     : U3  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list4* {unsetBreaks();} {noBreak();} list4 {unsetBreaks();})? ;
ulist4     : U4  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list5* {unsetBreaks();} {noBreak();} list5 {unsetBreaks();})? ;
ulist5     : U5  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list6* {unsetBreaks();} {noBreak();} list6 {unsetBreaks();})? ;
ulist6     : U6  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list7* {unsetBreaks();} {noBreak();} list7 {unsetBreaks();})? ;
ulist7     : U7  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list8* {unsetBreaks();} {noBreak();} list8 {unsetBreaks();})? ;
ulist8     : U8  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list9* {unsetBreaks();} {noBreak();} list9 {unsetBreaks();})? ;
ulist9     : U9  (WS ulist | olist | inline) LineBreak? ({yesBreak();} list10* {unsetBreaks();} {noBreak();} list10 {unsetBreaks();})? ;
ulist10    : U10 (WS ulist | olist | inline) ;

olist      : {yesBreak();} (olist1 LineBreak?)* {unsetBreaks();} {noBreak();} olist1 LineBreak? {unsetBreaks();};
olist1     : O1  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list2* {unsetBreaks();} {noBreak();} list2 {unsetBreaks();})? ;
olist2     : O2  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list3* {unsetBreaks();} {noBreak();} list3 {unsetBreaks();})? ;
olist3     : O3  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list4* {unsetBreaks();} {noBreak();} list4 {unsetBreaks();})? ;
olist4     : O4  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list5* {unsetBreaks();} {noBreak();} list5 {unsetBreaks();})? ;
olist5     : O5  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list6* {unsetBreaks();} {noBreak();} list6 {unsetBreaks();})? ;
olist6     : O6  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list7* {unsetBreaks();} {noBreak();} list7 {unsetBreaks();})? ;
olist7     : O7  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list8* {unsetBreaks();} {noBreak();} list8 {unsetBreaks();})? ;
olist8     : O8  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list9* {unsetBreaks();} {noBreak();} list9 {unsetBreaks();})? ;
olist9     : O9  (WS olist | ulist | inline) LineBreak? ({yesBreak();} list10* {unsetBreaks();} {noBreak();} list10 {unsetBreaks();})? ;
olist10    : O10 (WS olist | ulist | inline) ;

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

table      : {noBreak();} (trow (RowEnd | LineBreak))* trow (RowEnd | LineBreak)? {unsetBreaks();};
trow       : tcell+ ;
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

titlelink  : LiSt InLink? Sep InLink LiEnd ;

imglink    : ImSt InLink? Sep InLink ImEnd ;

simpleimg  : ImSt InLink ImEnd ;

wikiwlink  : WikiWords ;

attachment : Attachment ;

rawlink    : RawUrl ;

preformat  : NoWiki EndNoWikiInline ;

linebreak  : InlineBrk ({canBreak()}? LineBreak)? ;

macro      : MacroSt MacroName (MacroSep MacroEnd | MacroEndNoArgs) ;

any        : Any | WS | {canBreak()}? LineBreak ;

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