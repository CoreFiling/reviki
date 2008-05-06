package net.hillsdon.xml.xpath;

import javax.xml.xpath.XPathConstants;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.easymock.EasyMock.createMock;

public class TestCoercion extends TestCase {

  // Yes this is pretty much writing it twice but it saves me getting it wrong.
  public void testHaveNodesCorrespondingToStandardNodeTypes() {
    assertEquals(XPathConstants.NODE, Coercion.NODE.constant());
    assertEquals(XPathConstants.NODESET, Coercion.NODESET.constant());
    assertEquals(XPathConstants.NUMBER, Coercion.NUMBER.constant());
    assertEquals(XPathConstants.STRING, Coercion.STRING.constant());
    assertEquals(XPathConstants.BOOLEAN, Coercion.BOOLEAN.constant());
  }
  
  public void testElementReturnsElementForElement() throws Exception {
    Node element = EasyMock.createMock(Element.class);
    Element elementAgain = Coercion.ELEMENT.cast(createMock(XPathContext.class), element);
    assertSame(element, elementAgain);
  }
  
  public void testElementThrowsOnFailure() {
    try {
      Coercion.ELEMENT.cast(createMock(XPathContext.class), EasyMock.createMock(Node.class));
      fail();
    }
    catch (XPathReturnTypeException expected) {
    }
  }
  
}
