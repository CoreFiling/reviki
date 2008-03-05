package net.hillsdon.svnwiki.search;

/**
 * A match from a search.
 * 
 * Considered equal by page.
 * 
 * @author mth
 */
public class SearchMatch {

  private final String _page;
  private final String _htmlExtract;

  public SearchMatch(final String page, final String htmlExtract) {
    _page = page;
    _htmlExtract = htmlExtract;
  }

  /**
   * @return The page that matched.
   */
  public String getPage() {
    return _page;
  }
  
  /**
   * @return HTML extract showing match in context if requested and available,
   *         otherwise null.
   */
  public String getHtmlExtract() {
    return _htmlExtract;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof SearchMatch) {
      return _page.equals(((SearchMatch) obj)._page);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return _page.hashCode();
  }
  
}
