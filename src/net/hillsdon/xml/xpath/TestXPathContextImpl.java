package net.hillsdon.xml.xpath;

import java.util.Collections;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

import static net.hillsdon.xml.xpath.Coercion.CONTEXT;
import static net.hillsdon.xml.xpath.Coercion.STRING;

/**
 * We exercise it with a toy XML file.
 * 
 * @author mth
 */
public class TestXPathContextImpl extends TestCase {

  private XPathContext _document;

  @Override
  protected void setUp() throws Exception {
    _document = XPathContextFactory.newInstance().newXPathContext(new InputSource(getClass().getResource("toy.xml").toString()));
    _document.setNamespaceBindings(Collections.singletonMap("toy", "http://www.example.com/ns/toy"));
  }
  
  public void testNamespaceInformationCopiedToChildContexts() throws Exception {
    XPathContext manufacturer = _document.evaluate("toy:toy/toy:manufacturer", CONTEXT);
    assertEquals("Ted", manufacturer.evaluate("toy:name", STRING));
  }
  
  public void testElementCastFailing() throws Exception {
    assertNotNull(_document.evaluate("toy:toy/toy:components/@count", Coercion.NODE));
    try {
      _document.evaluate("toy:toy/toy:components/@count", Coercion.ELEMENT);
      fail();
    }
    catch (XPathReturnTypeException ex) {
    }
  }
  
}
