package net.hillsdon.svnwiki.wiki.xquery;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Empty implementations.
 * 
 * @author mth
 */
public final class NullErrorListener implements ErrorListener {
  
  public void error(final TransformerException ignored) throws TransformerException {
  }
  public void fatalError(final TransformerException ignored) throws TransformerException {
  }
  public void warning(final TransformerException ignored) throws TransformerException {
  }
  
}