package net.hillsdon.svnwiki.web;

public class Configuration {

  private String _url = null;

  public String getUrl() {
    return _url;
  }

  public void setUrl(final String url) {
    _url = url;
  }

  public boolean isComplete() {
    return _url != null;
  }

}
