package net.hillsdon.reviki.wiki.feeds;

/**
 * JAXP etc will drive you mad when used for testing otherwise.
 */
public class XPathAccessException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public XPathAccessException(final Throwable cause) {
    super(cause);
  }
  
}