package net.hillsdon.reviki.wiki.macros;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Tests for MacroArgumentParser.
 */
public class TestMacroArgumentParser extends TestCase {

    private MacroArgumentParser _parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _parser = new MacroArgumentParser("foo", "baz");
    }

    public void testNeedsBrackets() {
        try {
            _parser.parse("foo=\"bar\"");
            fail("Should throw exception if the query isn't surrounded in brackets");
        }
        catch (MacroArgumentParser.ParseException e) {
            //good
        }
    }

    public void testNeedsOpenBrackets() {
        try {
            _parser.parse("foo=\"bar\")");
            fail("Should throw exception if the query isn't surrounded in brackets");
        }
        catch (MacroArgumentParser.ParseException e) {
            //good
        }
    }

    public void testNeedsCloseBrackets() {
        try {
            _parser.parse("(foo=\"bar\"");
            fail("Should throw exception if the query isn't surrounded in brackets");
        }
        catch (MacroArgumentParser.ParseException e) {
            //good
        }
    }

    public void testFailsOnIncorrectArgName() {
        try {
            _parser.parse("(food=\"bar\")");
            fail("Should throw exception if the query contains unknown arg names");
        }
        catch (MacroArgumentParser.ParseException e) {
            //good
        }
    }

    public void testParseSingleArg() throws MacroArgumentParser.ParseException {
        Map<String, String> map = _parser.parse("(foo=\"bar\")");
        assertEquals("got incorrect value for only argument", "bar", map.get("foo"));
        assertEquals("parse result was wrong size", 1, map.size());
    }

    public void testParseTwoArgs() throws MacroArgumentParser.ParseException {
        Map<String, String> map = _parser.parse("(foo=\"bar\",baz=\"blort\")");
        assertEquals("got incorrect value for first argument", "bar", map.get("foo"));
        assertEquals("got incorrect value for second argument", "blort", map.get("baz"));
        assertEquals("parse result was wrong size", map.size(), 2);
    }

    public void testParseWithWhitespace() throws MacroArgumentParser.ParseException {
        Map<String, String> map = _parser.parse(" ( foo = \" bar \"  , baz = \"blort\" ) ");
        assertEquals("got incorrect value for first argument", " bar ", map.get("foo"));
        assertEquals("got incorrect value for second argument", "blort", map.get("baz"));
        assertEquals("parse result was wrong size", map.size(), 2);
    }

    public void testParseNestedBrackets() throws MacroArgumentParser.ParseException {
        Map<String, String> map = _parser.parse("(foo=\"bar(for foo)\",baz=\"blort\")");
        assertEquals("got incorrect value for first argument", "bar(for foo)", map.get("foo"));
        assertEquals("got incorrect value for second argument", "blort", map.get("baz"));
        assertEquals("parse result was wrong size", map.size(), 2);
    }

    public void testParseNestedEquals() throws MacroArgumentParser.ParseException {
        Map<String, String> map = _parser.parse("(foo=\"bar=good\",baz=\"blort\")");
        assertEquals("got incorrect value for first argument", "bar=good", map.get("foo"));
        assertEquals("got incorrect value for second argument", "blort", map.get("baz"));
        assertEquals("parse result was wrong size", map.size(), 2);
    }

    public void testParseNestedEqualsAndCommas() throws MacroArgumentParser.ParseException {
        Map<String, String> map = _parser.parse("(foo=\",bar=good\",baz=\"blort\")");
        assertEquals("got incorrect value for first argument", ",bar=good", map.get("foo"));
        assertEquals("got incorrect value for second argument", "blort", map.get("baz"));
        assertEquals("parse result was wrong size", map.size(), 2);
    }

}
