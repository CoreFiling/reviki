package net.hillsdon.reviki.wiki.feeds;

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Utility for testing XML documents using XPath.
 * 
 * Not written for production code but perhaps worth investing more effort in sometime.
 * 
 * @author mth
 */
public class XPathAccess {

  private static Document parse(final InputSource input) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document dom = dbf.newDocumentBuilder().parse(input);
      return dom;
    }
    catch (Exception ex) {
      throw new XPathAccessException(ex);
    }
  }
  
  private final Document _document;
  private XPath _xpath;

  public XPathAccess(InputSource input) {
    this(parse(input));
  }
  
  public XPathAccess(Document document) {
    _document = document;
    XPathFactory factory = XPathFactory.newInstance();
    _xpath = factory.newXPath(); 
  }

  public <T> T evaluate(final String expression, final ReturnAs<T> returnType) {
    return evaluate(_document, expression, returnType);
  }

  public <T> T evaluate(final Node node, final String expression, final ReturnAs<T> returnType) {
    try {
      XPathExpression expr = _xpath.compile(expression);
      return returnType.cast(expr.evaluate(node, returnType.constant()));
    }
    catch (XPathExpressionException ex) {
      throw new XPathAccessException(ex);
    }
  }

  public void setNamespaceBindings(final Map<String, String> prefixToNamespaceUri) {
    _xpath.setNamespaceContext(new NamespaceContext() {
      public String getNamespaceURI(final String prefix) {
        return prefixToNamespaceUri.get(prefix);
      }
      public String getPrefix(final String namespaceURI) {
        throw new UnsupportedOperationException();
      }
      @SuppressWarnings("unchecked")
      public Iterator getPrefixes(final String namespaceURI) {
        throw new UnsupportedOperationException();
      }
    });
  }
  
}
