package net.hillsdon.svnwiki.wiki.renderer;

import static net.hillsdon.svnwiki.text.WikiWordUtils.isWikiWord;

import java.util.regex.Matcher;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Links like c2:WikiPage and WikiPage but very generous as to
 * what counts as a wiki word.
 * 
 * @author mth
 */
public class CustomWikiLinkNode extends AbstractRegexNode {
  
  private static final Log LOG = LogFactory.getLog(CustomWikiLinkNode.class);
  
  private final InternalLinker _internalLinker;
  private final Configuration _configuration;
  
  public CustomWikiLinkNode(final InternalLinker internalLinker, final Configuration configuration) {
    super("(\\p{Alnum}+:)?(\\p{Alnum}+)");
    _internalLinker = internalLinker;
    _configuration = configuration;
  }

  @Override
  protected boolean confirmMatchFind(final Matcher matcher) {
    String wikiName = matcher.group(1);
    String pageName = matcher.group(2);
    boolean handle = wikiName != null || isWikiWord(pageName);
    //System.err.println(matcher.group(0) + " => " + handle);
    return handle;
  }

  public String handle(PageReference page, final Matcher result) {
    final String matched = result.group(0);
    String wikiName = result.group(1);
    String pageName = result.group(2);
    
    try {
        if (wikiName != null) {
          wikiName = wikiName.substring(0, wikiName.length() - 1);
        }
        if (wikiName == null) {
          return _internalLinker.link(pageName);
        }
        else {
          return interWikiLink(wikiName, pageName, matched);
        }
    }
    catch (UnknownWikiException ex) {
      // Fall through to default.
    }
    catch (PageStoreException ex) {
      LOG.error(ex);
    }
    return matched;
  }

  private String interWikiLink(final String wikiName, final String pageName, final String matched) throws UnknownWikiException, PageStoreException {
    String href = _configuration.getInterWikiLinker().link(wikiName, pageName);
    return String.format("<a class='inter-wiki' href='%s'>%s</a>", href, Escape.html(matched));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
  
}
