/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.text;

import static java.util.Arrays.asList;
import static net.hillsdon.reviki.text.WikiWordUtils.isWikiWord;
import static net.hillsdon.reviki.text.WikiWordUtils.pathToTitle;
import static net.hillsdon.reviki.text.WikiWordUtils.splitCamelCase;

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
    assertFalse(isWikiWord("12th"));
    assertFalse(isWikiWord(""));
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
  
  public void testDesiredButUnimplemented() {
    // The splits should model digits properly, digit-to-lower should
    // not be considered a step-down.
    assertSplits(asList("January", "The", "1", "2th"), "JanuaryThe12th");
    
    // Run of two uppercase should not count as word.
    assertSplits(asList("X", "Query"), "XQuery");
    assertSplits(asList("XML", "Query"), "XMLQuery");
    assertSplits(asList("The", "X", "Query", "Parser"), "TheXQueryParser");
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
