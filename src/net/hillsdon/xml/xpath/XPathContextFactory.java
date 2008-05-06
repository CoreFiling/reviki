package net.hillsdon.xml.xpath;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Call {@link XPathContextFactory#newInstance()}.
 * 
 * Exists to allow configuration options to be added as / when required.
 * 
 * Currently {@link DocumentBuilderFactory#setNamespaceAware(boolean)} is set
 * to true; no other options are provided.
 * 
 * @author mth
 */
public final class XPathContextFactory {
  
  public static XPathContextFactory newInstance() {
    return new XPathContextFactory();
  }

  private Document parse(final InputSource input) throws XPathContextInitializationException {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document dom = dbf.newDocumentBuilder().parse(input);
      return dom;
    }
    catch (Exception ex) {
      throw new XPathContextInitializationException(ex);
    }
  }
  
  /**
   * @param input Input.
   * @return An XPathContext based on a DOM built from the input 
   *         with the document as the context node.
   * @throws XPathContextInitializationException If we fail to parse.
   */
  public XPathContext newXPathContext(final InputSource input) throws XPathContextInitializationException {
    return newXPathContext(parse(input));
  }
  
  /**
   * @param context The context node.
   * @return An XPathContext with the given context node.
   */
  public XPathContext newXPathContext(final Node context) {
    return new XPathContextImpl(null, context);
  }
  
  private XPathContextFactory() {
  }
  
}
