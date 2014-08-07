package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import junit.framework.TestCase;

public class TestRawRenderer extends TestCase {
  PageInfo _page = new PageInfoImpl("", "FrontPage", "Hello World", Collections.<String, String> emptyMap());

  PageInfo _css = new PageInfoImpl("", "ConfigCss", "", Collections.<String, String> emptyMap());

  /** Check that we get out what we put in. */
  public void testRender() {
    RawRenderer renderer = new RawRenderer();
    Optional<InputStream> is = renderer.build(_page, URLOutputFilter.NULL);

    assertTrue(is.isPresent());

    try {
      String out = IOUtils.toString(is.get());
      assertTrue(out.equals(_page.getContent()));
    }
    catch (IOException e) {
      assertFalse("Failed to read from input stream", true);
    }
  }

  /** Check that the content type for ConfigCss is special. */
  public void testCssContentType() {
    RawRenderer renderer = new RawRenderer();
    renderer.render(_css);

    assertTrue(renderer.getContentType().equals("text/css"));
  }

  /** Check that the content type for non-ConfigCss pages is text. */
  public void testContentType() {
    RawRenderer renderer = new RawRenderer();
    renderer.render(_page);

    assertTrue(renderer.getContentType().equals("text/plain"));
  }
}
