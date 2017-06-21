package net.hillsdon.reviki.wiki.renderer.creole;

import java.io.IOException;

import net.hillsdon.reviki.wiki.renderer.RevikiRenderer;

import org.codehaus.jackson.JsonParseException;

import com.google.common.base.Optional;

public class TestCreole1Point0SpecExtracts extends JsonDrivenRenderingTest {

  public TestCreole1Point0SpecExtracts() throws JsonParseException, IOException {
    super(TestCreole1Point0SpecExtracts.class.getResource("spec-extracts.json"));
  }

  @Override
  protected String render(final String input) throws Exception {
    RevikiRenderer renderer = new RevikiRenderer(pageStore, linkHandler, imageHandler, macros);

    Optional<String> rendered = renderer.render(input);

    return rendered.isPresent() ? rendered.get() : "";
  }
}
