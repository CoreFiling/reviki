package net.hillsdon.svnwiki.wiki;

import net.hillsdon.svnwiki.configuration.InterWikiLinker;
import net.hillsdon.svnwiki.text.WikiWordUtils;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.filter.context.FilterContext;
import org.radeox.filter.regex.RegexTokenFilter;
import org.radeox.regex.MatchResult;
import org.radeox.util.Encoder;
import org.radeox.util.StringBufferWriter;

/**
 * Alternative to Radeox's link recognition.
 * 
 * @author mth
 */
public class CustomWikiLinkFilter extends RegexTokenFilter {

  public static final String INTERWIKI_LINKER_CONTEXT_KEY = "interwikiLinker";
  
  public CustomWikiLinkFilter() {
    super("([\\p{L}\\d]+:)?([\\p{L}\\d]+)");
  }

  /**
   * This is something of a hack... Radeox just blindly applies one
   * regex after another so we're at risk of crapping over URLs,
   * including hrefs we create etc.
   * 
   * @param buffer Current buffer.
   * @return True if we're in the middle of a URL.
   */
  private boolean inURL(final StringBuffer buffer) {
    int lastWs = buffer.lastIndexOf(" ");
    int lastUrl = Math.max(Math.max(buffer.lastIndexOf("http"), buffer.lastIndexOf("href=")), buffer.lastIndexOf("src="));
    int lastAttached = buffer.lastIndexOf("{attached|");
    if (lastUrl > lastWs || lastAttached > lastWs) {
      return true;
    }
    return false;
  }
  
  @Override
  public void handleMatch(final StringBuffer buffer, final MatchResult result, final FilterContext context) {
    final String matched = result.group(0);
    if (inURL(buffer)) {
      buffer.append(matched);
      return;
    }
    
    String wikiName = result.group(1);
    String pageName = result.group(2);
    
    RenderEngine engine = context.getRenderContext().getRenderEngine();
    StringBufferWriter writer = new StringBufferWriter(buffer);
    try {
      if (engine instanceof WikiRenderEngine) {
        WikiRenderEngine wikiEngine = (WikiRenderEngine) engine;
        if (wikiName != null) {
          wikiName = wikiName.substring(0, wikiName.length() - 1);
        }
        if (wikiName == null) {
          if (WikiWordUtils.isWikiWord(pageName)) {
            appendInternalLink(wikiEngine, buffer, pageName);
            return;
          }
        }
        else {
          InterWikiLinker interWikiLinker = (InterWikiLinker) context.getRenderContext().get(INTERWIKI_LINKER_CONTEXT_KEY);
          appendInterWikiLink(writer, wikiName, pageName, matched, interWikiLinker);
          return;
        }
      }
    }
    catch (UnknownWikiException ex) {
      // Fall through to default.
    }
    writer.write(matched);
  }

  private void appendInterWikiLink(final StringBufferWriter writer, final String wikiName, final String pageName, final String matched, final InterWikiLinker interwikiLinker) throws UnknownWikiException {
    String href = interwikiLinker.link(wikiName, pageName);
    writer.write(String.format("<a class='inter-wiki' href='%s'>%s</a>", Encoder.escape(href), matched));
  }

  private void appendInternalLink(final WikiRenderEngine wikiEngine, final StringBuffer buffer, final String pageName) {
    if (wikiEngine.exists(pageName)) {
      wikiEngine.appendLink(buffer, pageName, pageName);
    }
    else {
      wikiEngine.appendCreateLink(buffer, pageName, pageName);
    }
  }

}
