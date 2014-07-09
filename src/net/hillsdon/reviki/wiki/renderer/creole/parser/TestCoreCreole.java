package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.io.IOException;
import java.util.Collections;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.JsonDrivenRenderingTest;

import org.codehaus.jackson.JsonParseException;

public class TestCoreCreole extends JsonDrivenRenderingTest {
  public TestCoreCreole() throws JsonParseException, IOException {
    super(TestCoreCreole.class.getResource("../core-creole.json"));
  }

  @Override
  protected String render(final String input) {
    return CreoleRenderer.render(new PageInfoImpl("", "", input, Collections.<String, String> emptyMap()), URLOutputFilter.NULL, null).toXHTML();
  }
}
