/* Todo:
 *  - Inline html [<html>]foo[</html>]
 *  - Comments justifying and explaining every rule.
 *  - Allow arbitrarily-nested lists (see actions/attributes)
 */

parser grammar Creole;

options { tokenVocab=CreoleTokens; }

/* ***** Top level elements ***** */

creole    : (block ParBreak*)* EOF ;

block     : heading
          | ulist | olist
          | hrule
          | table
          | nowiki
          | paragraph
          ;

/* ***** Block Elements ***** */

heading   : HSt WS? inline HEnd? ;

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

table      : (trow LineBreak)* trow (LineBreak | EOF) ;
trow       : tcell+ CellSep?;
tcell      : th | td ;
th         : ThStart inline? ;
td         : CellSep inline? ;

nowiki     : NoWiki EndNoWikiBlock ;

/* ***** Inline Elements ***** */

inline     : inlinestep+ ;

inlinestep : bold | italic | sthrough
           | link | titlelink | imglink | wikiwlink
           | preformat
           | linebreak
           | any
           ;

bold       : BSt inline? BEnd ;

italic     : ISt inline? IEnd ;

sthrough   : SSt inline? SEnd ;

link       : LiSt InLink LiEnd ;

titlelink  : LiSt InLink Sep InLink LiEnd ;

imglink    : ImSt InLink Sep InLink ImEnd ;

wikiwlink  : WikiWords ;

preformat  : NoWiki EndNoWikiInline ;

linebreak  : InlineBrk LineBreak ;

any        : Any | WS | LineBreak ;
