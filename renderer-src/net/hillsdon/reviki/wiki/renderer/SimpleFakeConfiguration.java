package net.hillsdon.reviki.wiki.renderer;

import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InterWikiLinker;

public class SimpleFakeConfiguration implements Configuration {
  private final String _wikiName;
  private final String _baseUrl;

  public SimpleFakeConfiguration() {
    this("foo", "http://www.example.com/");
  }

  public SimpleFakeConfiguration(final String wikiName, final String baseUrl) {
    _wikiName = wikiName;
    _baseUrl = baseUrl;
  }

  public InterWikiLinker getInterWikiLinker() {
    final InterWikiLinker linker = new InterWikiLinker();
    linker.addWiki(_wikiName, _baseUrl + "%s");
    return linker;
  }
}
