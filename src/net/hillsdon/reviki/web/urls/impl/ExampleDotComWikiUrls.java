package net.hillsdon.reviki.web.urls.impl;

public class ExampleDotComWikiUrls extends AbstractWikiUrls {

  @Override
  protected String url(final String relative) {
    return "http://www.example.com/reviki" + relative;
  }

  public String pagesRoot() {
    return url("/pages/test-wiki/");
  }

}
