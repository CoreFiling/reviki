package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;

public class SvnWikiLinkPartHandler implements LinkPartHandler {
  
  private final InternalLinker _internalLinker;
  private final Configuration _configuration;

  public SvnWikiLinkPartHandler(final InternalLinker internalLinker, final Configuration configuration) {
    _internalLinker = internalLinker;
    _configuration = configuration;
  }
  
  public String handle(final PageReference page, final RenderNode renderer, final LinkParts link) {
    if (link.isURL()) {
      return link(page, renderer, "external", link.getRefd(), link.getText());
    }
    else {
      if (link.getWiki() != null) {
        try {
          return link(page, renderer, "inter-wiki", _configuration.getInterWikiLinker().url(link.getWiki(), link.getRefd()), link.getText());
        }
        catch (UnknownWikiException e) {
          return link.getText();
        }
        catch (PageStoreException e) {
          return link.getText();
        }
      }
      else {
        return _internalLinker.link(link.getRefd(), link.getText());
      }
    }
  }
  
  private String link(final PageReference page, final RenderNode renderer, final String clazz, final String url, final String text) {
    return String.format("<a class='%s' href='%s'>%s</a>", Escape.html(clazz), Escape.html(url), renderer.render(page, text));
  }

}
