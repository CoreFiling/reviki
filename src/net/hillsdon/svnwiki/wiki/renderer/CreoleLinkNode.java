package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.Matcher;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;

/**
 * "[["..."]]" links.
 * 
 * @author mth
 */
public class CreoleLinkNode extends AbstractRegexNode {
  
  private final InternalLinker _internalLinker;
  private final Configuration _configuration;

  public CreoleLinkNode(final InternalLinker internalLinker, final Configuration configuration) {
    super("\\[\\[(.*?)\\]\\]");
    _internalLinker = internalLinker;
    _configuration = configuration;
  }

  public String handle(final PageReference page, final Matcher matcher) {
    // [[wiki:PageName|Show this text]]
    // [[http://somewhere|Show this text]], so we have an ambiguity here.
    String group = matcher.group(1);
    int pipeIndex = group.lastIndexOf("|");
    int colonIndex = group.indexOf(":");
    
    String text = pipeIndex == -1 ? group : group.substring(pipeIndex + 1);
    String wiki = colonIndex == -1 ? null : group.substring(0, colonIndex);
    String refd = group.substring(colonIndex + 1, pipeIndex == -1 ? group.length() : pipeIndex);
    if (refd.startsWith("/")) {
      return link(page, wiki + ":" + refd, text);
    }
    else {
      if (wiki != null) {
        try {
          return link(page, _configuration.getInterWikiLinker().url(wiki, refd), text);
        }
        catch (UnknownWikiException e) {
          return group;
        }
        catch (PageStoreException e) {
          return group;
        }
      }
      else {
        return _internalLinker.link(refd, text);
      }
    }
  }

  private String link(final PageReference page, String url, String text) {
    return String.format("<a href='%s'>%s</a>", Escape.html(url), render(page, text));
  }
  
}
