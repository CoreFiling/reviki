package net.hillsdon.svnwiki.wiki;

import java.util.Arrays;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.util.Encoder;

/**
 * Customize the Radeox rendering engine to produce correct links for our wiki.
 * 
 * @author mth
 */
public class SvnWikiRenderEngine extends BaseRenderEngine implements WikiRenderEngine {

  private final PageStore _store;

  public SvnWikiRenderEngine(final PageStore store) {
    _store = store;
  }

  public boolean exists(final String name) {
    try {
      // TODO: make this more efficient!
      return Arrays.asList(_store.list()).contains(name);
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void appendCreateLink(final StringBuffer buffer, final String name, final String view) {
    buffer.append(String.format("<a class='new-page' href='%s'>%s</a>", Encoder.escape(name), Encoder.escape(view)));
  }

  public void appendLink(final StringBuffer buffer, final String name, final String view) {
    buffer.append(String.format("<a class='existing-page' href='%s'>%s</a>", Encoder.escape(name), Encoder.escape(view)));
  }

  public void appendLink(final StringBuffer buffer, final String name, final String view, final String anchor) {
    buffer.append(String.format("<a class='existing-page' href='%s#%s'>%s</a>", Encoder.escape(name), Encoder.escape(anchor), Encoder.escape(view)));
  }

  public boolean showCreate() {
    return true;
  }
  
}
