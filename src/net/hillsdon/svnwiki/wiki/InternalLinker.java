package net.hillsdon.svnwiki.wiki;

import static java.lang.String.format;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class InternalLinker {

  private final PageStore _store;

  public InternalLinker(PageStore store) {
    _store = store;
  }

  private boolean exists(final String name) {
    try {
      return _store.list().contains(name);
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String link(final String pageName) {
    if (exists(pageName)) {
      return format("<a class='new-page' href='%s'>%s</a>", Escape.url(pageName), Escape.html(pageName));
    }
    return format("<a class='existing-page' href='%s'>%s</a>", Escape.url(pageName), Escape.html(pageName));
  }
  
}
