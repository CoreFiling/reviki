package net.hillsdon.xml.xpath;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.easymock.EasyMock.createMock;

/**
 * Test for {@link ListCoercion}.
 * 
 * @author mth
 */
public class TestElementsCoercion extends TestCase {

  private static class ListNodeList implements NodeList {

    private final List<Node> _list;

    public ListNodeList(final List<Node> list) {
      _list = list;
      
    }
    
    public int getLength() {
      return _list.size();
    }

    public Node item(int index) {
      return _list.get(index);
    }
    
  }
  
  public void testAllElementsReturnsList() throws Exception {
    List<Node> expected = Arrays.<Node>asList(createMock(Element.class), createMock(Element.class));
    assertEquals(expected, elementsCast(expected));
  }
  
  public void testIndividualCastFailsSoOverallCastFails() throws Exception {
    try {
      elementsCast(Arrays.<Node>asList(createMock(Element.class), createMock(Node.class)));
      fail();
    }
    catch (XPathReturnTypeException expected) {
    }
  }

  private List<Element> elementsCast(List<Node> input) throws XPathReturnTypeException {
    return Coercion.ELEMENTS.cast(createMock(XPathContext.class), new ListNodeList(input));
  }
  
}
