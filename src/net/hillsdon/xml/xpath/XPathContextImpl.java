package net.hillsdon.xml.xpath;

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Node;

/**
 * Utility for testing XML documents using XPath.
 * 
 * @author mth
 */
public class XPathContextImpl implements XPathContext {

  private final Node _context;
  private final XPath _xpath;

  public XPathContextImpl(final XPathContext parent, final Node context) {
    _context = context;
    XPathFactory factory = XPathFactory.newInstance();
    _xpath = factory.newXPath();
    if (parent != null) {
      copyConfiguration(parent);
    }
  }

  private void copyConfiguration(final XPathContext fromContext) {
    final XPath from = fromContext.getXPath();
    final NamespaceContext namespaceContext = from.getNamespaceContext();
    final XPathFunctionResolver xpathFunctionResolver = from.getXPathFunctionResolver();
    final XPathVariableResolver xpathVariableResolver = from.getXPathVariableResolver();
    if (namespaceContext != null) {
      _xpath.setNamespaceContext(namespaceContext);
    }
    if (xpathFunctionResolver != null) {
      _xpath.setXPathFunctionResolver(xpathFunctionResolver);
    }
    if (xpathVariableResolver != null) {
      _xpath.setXPathVariableResolver(xpathVariableResolver);
    }
  }

  public <T> T evaluate(final String expression, final Coercion<T> returnType) throws XPathExpressionException, XPathReturnTypeException {
    Object result = _xpath.evaluate(expression, _context, returnType.constant());
    return returnType.cast(this, result);
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

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{context:" + _context + "}";
  }

  public Node getContextNode() {
    return _context;
  }

  public XPath getXPath() {
    return _xpath;
  }
  
}
