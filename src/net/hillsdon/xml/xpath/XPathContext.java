package net.hillsdon.xml.xpath;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

/**
 * XPath evaluation from a context node.
 * 
 * @see XPathContextFactory
 * 
 * @author mth
 */
public interface XPathContext {

  /**
   * @param <T> The return type.
   * @param expression An XPath expression.
   * @param returnType Determines return type.
   * @return The result of the evaluation.
   * 
   * @throws XPathExpressionException If the XPath expression doesn't compile / evaluate.
   * @throws XPathReturnTypeException If the coercion fails (can only happen with custom coercions). 
   */
  <T> T evaluate(final String expression, final Coercion<T> returnType) throws XPathExpressionException, XPathReturnTypeException;

  /**
   * @return The context node.
   */
  Node getContextNode();
  
  /**
   * @return The underlying XPath object.
   */
  XPath getXPath();
  
  /**
   * Sets a {@link NamespaceContext} based on the given map.
   * 
   * @param prefixToNamespaceUri Map from prefix to namespace.  Possibly empty, never null. 
   */
  void setNamespaceBindings(final Map<String, String> prefixToNamespaceUri);

}