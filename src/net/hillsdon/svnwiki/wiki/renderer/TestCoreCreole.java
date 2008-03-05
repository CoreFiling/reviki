package net.hillsdon.svnwiki.wiki.renderer;

import java.io.IOException;

import net.hillsdon.svnwiki.vc.PageReference;

import org.codehaus.jackson.JsonParseException;

public class TestCoreCreole extends JsonDrivenRenderingTest {

  public TestCoreCreole() throws JsonParseException, IOException {
    super(TestCoreCreole.class.getResource("core-creole.json"));
  }

  @Override
  protected String render(final String input) {
    return new CreoleRenderer(CreoleRenderer.NONE, CreoleRenderer.NONE).render(new PageReference(""), input);
  }
  
}
