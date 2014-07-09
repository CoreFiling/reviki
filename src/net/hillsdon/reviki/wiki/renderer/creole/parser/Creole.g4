/* Todo:
 *  - Inline html [<html>]foo[</html>]
 *  - Comments justifying and explaining every rule.
 *  - Tidy up heading matching (see semantic predicates)
 *  - Allow arbitrarily-nested lists (see actions/attributes)
 */

parser grammar Creole;

options { tokenVocab=CreoleTokens; }

/* ***** Top level elements ***** */

creole    : (block ParBreak+)* EOF | block ParBreak* EOF;

block     : heading
          | ulist | olist
          | hrule
          | table
          | nowiki
          | paragraph
          ;

/* ***** Block Elements ***** */

heading   : heading1 | heading2 | heading3
          | heading4 | heading5 | heading6
          ;
heading1  : H1 inline HEnd? ;
heading2  : H2 inline HEnd? ;
heading3  : H3 inline HEnd? ;
heading4  : H4 inline HEnd? ;
heading5  : H5 inline HEnd? ;
heading6  : H6 inline HEnd? ;

paragraph : inline ;

ulist      : ulist1+ ;
ulist1     : U1 inline ulist2* ;
ulist2     : U2 inline ulist3* ;
ulist3     : U3 inline ulist4* ;
ulist4     : U4 inline ulist5* ;
ulist5     : U5 inline ;

olist      : olist1+ ;
olist1     : O1 inline olist2* ;
olist2     : O2 inline olist3* ;
olist3     : O3 inline olist4* ;
olist4     : O4 inline olist5* ;
olist5     : O5 inline ;

hrule      : Rule ;

table      : trow+ ;
trow       : (tcell+ CellSep)? LineBreak ;
tcell      : th | td ;
th         : ThStart inline ;
td         : TdStart inline ;

nowiki     : NoWiki AnyText EndNoWiki ;

/* ***** Inline Elements ***** */

inline     : inlinestep+ ;

inlinestep : bold | italic | sthrough
           | link | titlelink | imglink | wikiwlink
           | preformat
           | linebreak
           | any
           ;

bold       : Bold inline? Bold ;

italic     : Italic inline? Italic ;

sthrough   : Sthrough inline? Sthrough ;

link       : LiSt InLink LiEnd ;

titlelink  : LiSt InLink Sep InLink LiEnd ;

imglink    : ImSt InLink Sep InLink ImEnd ;

wikiwlink  : WikiWords ;

preformat  : NoWikiInline AnyInlineText EndNoWikiInline ;

linebreak  : InlineBrk LineBreak ;

any        : Any ;