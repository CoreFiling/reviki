package net.hillsdon.svnwiki.search;


public class SearchMatch {

  private final String _page;
  private final String _htmlExtract;

  public SearchMatch(final String page, final String htmlExtract) {
    _page = page;
    _htmlExtract = htmlExtract;
  }

  public String getPage() {
    return _page;
  }
  
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
