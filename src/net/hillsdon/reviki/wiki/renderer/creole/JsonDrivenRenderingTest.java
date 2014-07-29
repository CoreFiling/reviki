/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.wiki.renderer.creole;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.reviki.configuration.AbstractPropertiesStore;
import net.hillsdon.reviki.configuration.DataDir;
import net.hillsdon.reviki.configuration.PersistentStringMap;
import net.hillsdon.reviki.configuration.PropertiesDeploymentConfiguration;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.ApplicationUrlsImpl;
import net.hillsdon.reviki.wiki.renderer.HtmlRenderer;
import net.hillsdon.reviki.wiki.renderer.SvnWikiLinkPartHandler;
import net.hillsdon.reviki.wiki.renderer.XHTML5Validator;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JavaTypeMapper;
import org.xml.sax.InputSource;

public abstract class JsonDrivenRenderingTest extends TestCase {

  private static final String HTML_PREFIX = "<!DOCTYPE html>"
                                           + "<html xmlns='http://www.w3.org/1999/xhtml'><head><title>HTML prefix</title></head><body>";
  private static final String HTML_SUFFIX = "</body></html>";
  private final XHTML5Validator _validator = new XHTML5Validator();
  private final List<Map<String, String>> _tests;

  protected byte[] _bytes = new byte[0];

  protected boolean _persistable = true;

  protected final PersistentStringMap _properties = new AbstractPropertiesStore() {
    protected InputStream inputStream() throws IOException {
      return new ByteArrayInputStream(_bytes);
    }

    protected OutputStream outputStream() throws IOException {
      return new ByteArrayOutputStream() {
        @Override
        public void close() throws IOException {
          _bytes = toByteArray();
        }
      };
    }

    public boolean isPersistable() {
      return _persistable;
    }
  };

  protected LinkPartsHandler linkHandler, imageHandler;

  @SuppressWarnings("unchecked")
  public JsonDrivenRenderingTest(final URL url) throws JsonParseException, IOException {
    JsonFactory jf = new JsonFactory();
    _tests = (List<Map<String, String>>) new JavaTypeMapper().read(jf.createJsonParser(url));

    DataDir _dataDir = createMock(DataDir.class);
    expect(_dataDir.getProperties()).andReturn(_properties);
    replay(_dataDir);

    ApplicationUrlsImpl urls = new ApplicationUrlsImpl("", new PropertiesDeploymentConfiguration(_dataDir));
    WikiUrls wikiUrls = urls.get("test");

    InternalLinker linker = new InternalLinker(wikiUrls);
    InterWikiLinker wikilinker = new InterWikiLinker();
    wikilinker.addWiki("Ohana", "http://wikiohana.net/cgi-bin/wiki.pl/%s");
    PageStore store = new SimplePageStore();

    LinkResolutionContext resolver = new LinkResolutionContext(linker, wikilinker, store);

    linkHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, resolver);
    imageHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, resolver);
  }

  public void test() throws Exception {
    final PrintStream err = System.err;
    int errors = 0;
    for (Map<String, String> test : _tests) {
      final String caseName = test.get("name");
      final String bugExplanation = test.get("bug");
      final String expected = test.get("output");
      final String input = test.get("input");
      final String actual = render(input);
      
      // We ignore the CSS class we add to save cluttering the expectations.
      String tidiedActual = actual.replaceAll(" " + HtmlRenderer.CSS_CLASS_ATTR, "");
      final boolean match = expected.equals(tidiedActual);
      if (bugExplanation != null) {
        assertFalse("You fixed " + caseName, match);
        continue;
      }
      if (!match) {
        errors++;
        err.println("Creole case: " + caseName);
        err.println("Input:\n" + input);
        err.println("Expected:\n" + expected);
        err.println("Actual (tidied):\n" + tidiedActual);
        err.println();
      }
      validate(caseName, actual);
    }
    if (errors > 0) {
      fail("Rendering errors, please see stderr.");
    }
  }

  private void validate(final String caseName, final String actual) {
    // Put the content in a <body> tag first.
    final String content = HTML_PREFIX + actual + HTML_SUFFIX;
    try {
      _validator.validate(new InputSource(new StringReader(content)));
    }
    catch (IOException e) {
      throw new RuntimeException(String.format("Failed to read: %s", content));
    }
  }

  protected abstract String render(String input) throws Exception;
  
}
