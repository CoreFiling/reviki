package net.hillsdon.svnwiki.wiki.xquery;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public final class NoFileSchemeURIResolver implements URIResolver {
  
  public Source resolve(final String href, final String base) throws TransformerException {
    try {
      URI hrefURI = href == null ? null : new URI(href);
      URI baseURI = base == null ? null : new URI(base);
      URI resolved;
      if (base == null) {
        resolved = hrefURI;
      }
      else {
        if (href == null) {
          resolved = baseURI;
        }
        else {
          resolved = baseURI.resolve(hrefURI);
        }
      }
      if (!resolved.isAbsolute()) {
        throw new TransformerException("Could not resolve URI.");
      }
      if (resolved.getScheme().equalsIgnoreCase("file")) {
        throw new TransformerException("'file' scheme URIs not permitted.");
      }
    }
    catch (URISyntaxException e) {
      throw new TransformerException(e);
    }
    // Let Saxon do the real work.
    return null;
  }
  
}