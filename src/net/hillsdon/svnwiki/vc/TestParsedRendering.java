package net.hillsdon.svnwiki.vc;

import java.io.StringReader;

import junit.framework.TestCase;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TestParsedRendering extends TestCase {

  public void test() throws Exception {
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(new StringReader("<p>Hello</p>")));
    Document document = parser.getDocument();
    assertEquals(1, document.getElementsByTagName("p").getLength());
  }
  
}
