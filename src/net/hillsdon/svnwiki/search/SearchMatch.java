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
  
}
