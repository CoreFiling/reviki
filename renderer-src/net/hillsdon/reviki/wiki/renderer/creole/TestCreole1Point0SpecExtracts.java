package net.hillsdon.reviki.wiki.renderer.creole;

import java.io.IOException;
import java.util.Collections;

import net.hillsdon.reviki.vc.impl.DummyPageStore;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.HtmlRenderer;

import org.codehaus.jackson.JsonParseException;

import com.google.common.base.Optional;

public class TestCreole1Point0SpecExtracts extends JsonDrivenRenderingTest {

  public TestCreole1Point0SpecExtracts() throws JsonParseException, IOException {
    super(TestCreole1Point0SpecExtracts.class.getResource("spec-extracts.json"));
  }

  @Override
  protected String render(final String input) throws Exception {
    DummyPageStore pages = new DummyPageStore();
    pages.set(new PageInfoImpl(null, "ExistingPage", "Content", Collections.<String, String>emptyMap()), "", -1, "");
    pages.set(new PageInfoImpl(null, "ExistingPage1.1", "Content", Collections.<String, String>emptyMap()), "", -1, "");

    HtmlRenderer renderer = new HtmlRenderer(pages, linkHandler, imageHandler, macros);

    Optional<String> rendered = renderer.render(new PageInfoImpl("", "", input, Collections.<String, String> emptyMap()), URLOutputFilter.NULL);

    return rendered.isPresent() ? rendered.get() : "";
  }
}
