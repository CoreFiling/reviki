package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkParts;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.svnwiki.wiki.renderer.creole.RenderNode;

public class SvnWikiLinkPartHandler implements LinkPartsHandler {
  
  public static final String IMAGE = "<img class='%s' src='%s' alt='%s' />";
  public static final String ANCHOR = "<a class='%s' href='%s'>%s</a>";
  
  private final InternalLinker _internalLinker;
  private final Configuration _configuration;
  private final String _formatString;

  public SvnWikiLinkPartHandler(final String formatString, final InternalLinker internalLinker, final Configuration configuration) {
    _formatString = formatString;
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
        // Add page prefix if it is an attachment for the current page.
        String refd = link.getRefd();
        boolean hasPagePart = refd.contains("/");
        if (refd.contains(".") || hasPagePart) {
          if (!hasPagePart) {
            refd = page.getPath() + "/attachments/" + refd;
          }
          else {
            refd = refd.replaceFirst("/", "/attachments/");
          }
          return link(page, renderer, "attachment", refd, link.getText());
        }
        else {
          return _internalLinker.link(link.getRefd(), link.getText());
        }
      }
    }
  }
  
  private String link(final PageReference page, final RenderNode renderer, final String clazz, final String url, final String text) {
    return String.format(_formatString, Escape.html(clazz), Escape.html(url), renderer.render(page, text));
  }

}
