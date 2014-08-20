package net.hillsdon.reviki.wiki.renderer.creole;

import java.io.IOException;

import net.hillsdon.reviki.wiki.renderer.HtmlRenderer;

import org.codehaus.jackson.JsonParseException;

import com.google.common.base.Optional;

public class TestCoreCreole extends JsonDrivenRenderingTest {
  public TestCoreCreole() throws JsonParseException, IOException {
    super(TestCoreCreole.class.getResource("core-creole.json"));
  }

  @Override
  protected String render(final String input) throws Exception {
    HtmlRenderer renderer = new HtmlRenderer(pageStore, linkHandler, imageHandler, macros);

    Optional<String> rendered = renderer.render(input);

    return rendered.isPresent() ? rendered.get() : "";
  }
}
