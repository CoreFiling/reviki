package net.hillsdon.xml.xpath;


/**
 * In general failure to set up the parse of a DOM document is not recoverable
 * so this is a runtime exception.
 */
public class XPathContextInitializationException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public XPathContextInitializationException(final Throwable cause) {
    super(cause);
  }
  
}