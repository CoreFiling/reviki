package net.hillsdon.svnwiki.text;

import static java.util.Arrays.asList;
import static net.hillsdon.svnwiki.text.WikiWordUtils.isWikiWord;
import static net.hillsdon.svnwiki.text.WikiWordUtils.pathToTitle;
import static net.hillsdon.svnwiki.text.WikiWordUtils.splitCamelCase;

import java.util.List;

import junit.framework.TestCase;

public class TestWikiWordUtils extends TestCase { 

  public void testIsWikiWord() {
    assertFalse(isWikiWord("IT"));
    assertTrue(isWikiWord("HTMLParser"));
    assertFalse(isWikiWord("HTML Parser"));
    assertTrue(isWikiWord("HTML"));
    assertFalse(isWikiWord("parser"));
    assertFalse(isWikiWord("parserGenerator"));
  }
  
  public void testOddCharacters() {
    String happyu = "JürgenHabermas";
    assertEquals(asList("Jürgen", "Habermas"), splitCamelCase(happyu));
    assertEquals("Jürgen Habermas", pathToTitle(happyu));
  }
  
  public void testLabel() {
    assertEquals("My First Property", pathToTitle("MyFirstProperty"));
    assertEquals("my First Property", pathToTitle("object/myFirstProperty"));
    assertEquals("lemon Cheesecake", pathToTitle("lemonCheesecake"));
  }
  
  public void testDegenerate() {
    assertSplits(asList(""), "");
    assertSplits(asList("word"), "word");
    assertSplits(asList("Word"), "Word");
  }

  public void testSplitsSimpleCamelCase() {
    assertSplits(asList("test", "Splits", "Simple", "Camel", "Case"), "testSplitsSimpleCamelCase");
  }

  public void testNumbers() {
    assertSplits(asList("1"), "1");    
    assertSplits(asList("only", "1"), "only1");    
    assertSplits(asList("1", "And", "Only"), "1AndOnly");    
    assertSplits(asList("remove", "1st", "Egg", "From", "Basket"), "remove1stEggFromBasket");
    assertSplits(asList("remove", "1", "Egg", "From", "Basket"), "remove1EggFromBasket");    
    assertSplits(asList("enter", "Room", "101"), "enterRoom101");    
  }
  
  public void testAcronyms() {
    assertSplits(asList("Test", "MRG", "Page"), "TestMRGPage");
    assertSplits(asList("HTML", "Parser"), "HTMLParser");
    assertSplits(asList("Simple", "HTML", "Parser"), "SimpleHTMLParser");
    assertSplits(asList("Parses", "HTML"), "ParsesHTML");
    assertSplits(asList("TTFN", "You", "TLA", "Lover"), "TTFNYouTLALover");
    assertSplits(asList("IANAL"), "IANAL");
    // Note the way your brain parses it isn't camel case.
    assertSplits(asList("ON", "Eor", "TWO"), "ONEorTWO");
  }
  
  private void assertSplits(List<String> expected, String input) {
    assertEquals(expected, splitCamelCase(input));
  }
  
}
