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
  
  @Override
  public void handleMatch(final StringBuffer buffer, final MatchResult result, final FilterContext context) {
    final String matched = result.group(0);
    RenderEngine engine = context.getRenderContext().getRenderEngine();
    StringBufferWriter writer = new StringBufferWriter(buffer);
    try {
      if (engine instanceof WikiRenderEngine) {
        WikiRenderEngine wikiEngine = (WikiRenderEngine) engine;
        String wikiName = result.group(1);
        if (wikiName != null) {
          wikiName = wikiName.substring(0, wikiName.length() - 1);
        }
        String pageName = result.group(2);
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
