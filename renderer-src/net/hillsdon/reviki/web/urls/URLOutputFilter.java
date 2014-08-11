package net.hillsdon.reviki.web.urls;


/**
 * Most of the time we want to use jsession ids on URLs if cookies
 * are disabled.  That requires the response to encode the URL.
 * 
 * In some contexts (e.g. indexing) that isn't appropriate so we hide
 * the switch behind this interface. 
 * 
 * @author mth
 */
public interface URLOutputFilter {

  public static final URLOutputFilter NULL = new URLOutputFilter() {    
    public String filterURL(final String url)
    {
      return url;
    }
  };

  String filterURL(String url);

}
