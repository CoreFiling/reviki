package net.hillsdon.reviki.wiki.renderer;

import com.google.common.base.Function;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.wiki.renderer.creole.RenderingTest;

import java.util.Collections;
import java.util.Map;

public class TestDelegatingRenderer extends RenderingTest {

  private Function<String, String> _defaultSyntax;
  private DelegatingRenderer _renderer;

  public void setUp() throws Exception {
    _defaultSyntax = new Function<String, String>() {
      @Override
      public String apply(String filename) {
        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : null;
      }
    };
    _renderer = new DelegatingRenderer(pageStore, linkHandler, imageHandler, macros, _defaultSyntax);
  }

  public void testRenderExplictlyMarkdown() throws Exception {
    assertEquals("<h1>Markdown</h1>", render("file.txt", "# Markdown", "markdown"));
  }

  public void testRenderExplictlyReviki() throws Exception {
    assertEquals("<h1>Reviki</h1>", render("file.txt", "= Reviki", "reviki"));
  }

  public void testRenderUnknownFormatDefaultsToReviki() throws Exception {
    assertEquals("<h1>Other</h1>", render("file.txt", "= Other", "other"));
  }

  public void testRenderUsesWikiDefault() throws Exception {
    assertEquals("<h1>Default Reviki</h1>", render("file.reviki", "= Default Reviki", null));
    assertEquals("<h1>Default Markdown</h1>", render("file.markdown", "# Default Markdown", null));
  }

  public void testRenderDefaultsToRevikiIfNoWikiDefault() throws Exception {
    assertEquals("<h1>Default Reviki</h1>", render("file", "= Default Reviki", null));
  }

  private String render(final String path, final String content, final String syntax) {
    final Map<String, String> attributes = syntax == null ? Collections.<String, String>emptyMap() : Collections.singletonMap("syntax", syntax);
    String output = _renderer.render(new PageInfoImpl("", path, content, attributes)).get();
    return output.replaceFirst("\\s*class='wiki-content'\\s*", "").replaceAll("\n", "");
  }
}