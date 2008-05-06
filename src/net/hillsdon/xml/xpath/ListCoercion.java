package net.hillsdon.xml.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.NodeList;

public class ListCoercion<T> extends Coercion<List<T>> {

  private final Coercion<T> _individual;

  public ListCoercion(final Coercion<T> individual) {
    super(XPathConstants.NODESET);
    _individual = individual;
  }
  
  @Override
  public List<T> cast(XPathContext context, final Object o) throws XPathReturnTypeException {
    // We could implement list in terms of NodeList but that would defer the class casts.
    final NodeList nodeList = NODESET.cast(context, o);
    final int length = nodeList.getLength();
    final List<T> nodes = new ArrayList<T>(length);
    for (int i = 0; i < length; ++i) {
      nodes.add(_individual.cast(context, nodeList.item(i)));
    }
    return nodes;
  }
  
}
