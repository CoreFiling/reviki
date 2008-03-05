package net.hillsdon.svnwiki.wiki;

import static java.lang.String.format;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class InternalLinker {

  private final PageStore _store;
  private final String _wikiName;
  private final String _contextPath;

  public InternalLinker(final String contextPath, String wikiName, final PageStore store) {
    _contextPath = contextPath;
    _wikiName = wikiName;
    _store = store;
  }

  private boolean exists(final String name) {
    try {
      return _store.list().contains(new PageReference(name));
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String url(final String pageName) {
    return String.format("%s/pages/%s%s", 
      _contextPath,
      _wikiName != null ? Escape.url(_wikiName) + "/" : "", 
      Escape.url(pageName)
    );
  }
  
  public String link(final String pageName) {
    String cssClass = exists(pageName) ? "existing-page" : "new-page";
    return format("<a class='%s' href='%s'>%s</a>",
      cssClass,
      Escape.html(url(pageName)),
      Escape.html(pageName)
    );
  }
  
}
