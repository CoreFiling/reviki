package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.io.IOException;
import java.util.Collections;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.JsonDrivenRenderingTest;

import org.codehaus.jackson.JsonParseException;

public class TestCreole1Point0SpecExtracts extends JsonDrivenRenderingTest {

  public TestCreole1Point0SpecExtracts() throws JsonParseException, IOException {
    super(TestCreole1Point0SpecExtracts.class.getResource("../spec-extracts.json"));
  }

  @Override
  protected String render(final String input) {
    return CreoleRenderer.render(new PageInfoImpl("", "", input, Collections.<String, String> emptyMap()), URLOutputFilter.NULL, null).toXHTML();
  }
}
