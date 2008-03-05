package net.hillsdon.svnwiki.wiki.renderer;

import java.io.IOException;
import java.io.StringWriter;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.SimplePageStore;
import net.hillsdon.svnwiki.wiki.InternalLinker;

import org.codehaus.jackson.JsonParseException;

public class TestRenderingExtensions extends JsonDrivenRenderingTest {

  public TestRenderingExtensions() throws JsonParseException, IOException {
    super(TestRenderingExtensions.class.getResource("rendering-extensions.json"));
  }

  @Override
  protected String render(final String input) throws IOException, PageStoreException {
    CreoleMarkupRenderer renderer = new CreoleMarkupRenderer(new FakeConfiguration(), new InternalLinker("", "mywiki", new SimplePageStore()));
    final StringWriter out = new StringWriter();
    renderer.render(new PageReference(""), input, out);
    return out.toString();
  }
  
}
