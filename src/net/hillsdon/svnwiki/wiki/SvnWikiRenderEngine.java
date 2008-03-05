package net.hillsdon.svnwiki.wiki;

import java.util.Arrays;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.util.Encoder;

public class SvnWikiRenderEngine extends BaseRenderEngine implements WikiRenderEngine {

  private final PageStore _store;

  public SvnWikiRenderEngine(final PageStore store) {
    _store = store;
  }

  @Override
  public boolean exists(final String name) {
    System.err.println("exists? " + name);
    try {
      // TODO: make this more efficient!
      return Arrays.asList(_store.list()).contains(name);
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void appendCreateLink(final StringBuffer buffer, final String name, final String view) {
    System.err.println("Called: " + name + " " + view);
    appendLink(buffer, name, view);
  }

  @Override
  public void appendLink(final StringBuffer buffer, final String name, final String view) {
    System.err.println("Called: " + name + " " + view);
    buffer.append(String.format("<a href='%s'>%s</a>", Encoder.escape(name), Encoder.escape(view)));
  }

  @Override
  public void appendLink(final StringBuffer buffer, final String name, final String view, final String anchor) {
    System.err.println("Called: " + name + " " + view);
    buffer.append(String.format("<a href='%s#%s'>%s</a>", Encoder.escape(name), Encoder.escape(anchor), Encoder.escape(view)));
  }

  @Override
  public boolean showCreate() {
    return true;
  }
  
}
