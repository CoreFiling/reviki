package net.hillsdon.reviki.wiki.renderer.creole;

import java.io.IOException;
import java.util.Collections;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.renderer.FakeConfiguration;
import net.hillsdon.reviki.wiki.renderer.SvnWikiLinkPartHandler;

import org.codehaus.jackson.JsonParseException;

public class TestCreole1Point0SpecExtracts extends JsonDrivenRenderingTest {

  public TestCreole1Point0SpecExtracts() throws JsonParseException, IOException {
    super(TestCreole1Point0SpecExtracts.class.getResource("spec-extracts.json"));
  }

  @Override
  protected String render(final String input) throws Exception {
    SimplePageStore pages = new SimplePageStore();
    pages.set(new PageInfoImpl(null, "ExistingPage", "Content", Collections.<String, String>emptyMap()), "", -1, "");
    pages.set(new PageInfoImpl(null, "ExistingPage1.1", "Content", Collections.<String, String>emptyMap()), "", -1, "");

    return CreoleRenderer.render(
        pages,
        new PageInfoImpl("", "", input, Collections.<String, String> emptyMap()),
        URLOutputFilter.NULL,
        new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, pages, new InternalLinker(new ExampleDotComWikiUrls()), new FakeConfiguration()),
        new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, pages, new InternalLinker(new ExampleDotComWikiUrls()), new FakeConfiguration())
        ).toXHTML();
  }
}
