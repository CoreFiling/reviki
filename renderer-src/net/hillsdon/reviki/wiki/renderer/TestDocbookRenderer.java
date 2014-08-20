package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.wiki.renderer.creole.JsonDrivenRenderingTest;

public class TestDocbookRenderer extends JsonDrivenRenderingTest {

  private DocbookRenderer _renderer;

  public TestDocbookRenderer() throws JsonParseException, IOException {
    super(TestRenderingExtensions.class.getResource("docbook.json"));

    _renderer = new DocbookRenderer(pageStore, linkHandler, imageHandler, macros);
  }

  @Override
  protected String render(String input) throws Exception {
    InputStream is = _renderer.render(new PageInfoImpl("", "TestPage", input, Collections.<String, String> emptyMap())).get();
    String docbook = IOUtils.toString(is);

    // Strip off the XML declaration and article tag.
    docbook = docbook.substring("<?xml version=\"1.0\" encoding=\"utf-8\"?>".length());
    docbook = docbook.substring("<article xmlns=\"http://docbook.org/ns/docbook\" xmlns:xl=\"http://www.w3.org/1999/xlink\" version=\"5.0\" xml:lang=\"en\">\n".length());
    docbook = docbook.substring(0, docbook.length() - "\n</article>".length() - 1);

    return docbook;
  }

  @Override
  protected void validate(String caseName, String actual) {
    // The XMl is going to be valid as long as the API is.
  }

  /** Check we have an XML content type. */
  public void testContentType() {
    assertTrue(_renderer.getContentType().startsWith("text/xml"));
  }
}
