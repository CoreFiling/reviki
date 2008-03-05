package net.hillsdon.svnwiki.wiki.renderer.creole;

import java.io.IOException;

import net.hillsdon.svnwiki.vc.PageReference;

import org.codehaus.jackson.JsonParseException;

public class TestCreole1Point0SpecExtracts extends JsonDrivenRenderingTest {

  public TestCreole1Point0SpecExtracts() throws JsonParseException, IOException {
    super(TestCreole1Point0SpecExtracts.class.getResource("spec-extracts.json"));
  }

  @Override
  protected String render(final String input) {
    return new CreoleRenderer(CreoleRenderer.NONE, CreoleRenderer.NONE).render(new PageReference(""), input);
  }
  
}
