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
package net.hillsdon.reviki.wiki.feeds;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.StoreKind;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.xml.xpathcontext.XPathContext;
import net.hillsdon.xml.xpathcontext.XPathContextFactory;

import org.xml.sax.InputSource;

import static net.hillsdon.xml.xpathcontext.Coercion.CONTEXT;
import static net.hillsdon.xml.xpathcontext.Coercion.NUMBER;
import static net.hillsdon.xml.xpathcontext.Coercion.STRING;

public class TestFeedWriter extends TestCase {

  public void test() throws Exception {
    StringWriter out = new StringWriter();
    List<ChangeInfo> changes = Arrays.asList(new ChangeInfo("SomeWikiPage", "SomeWikiPage", "mth", new Date(0), 123, "Change description", StoreKind.PAGE, ChangeType.MODIFIED, null, -1));
    WikiUrls urls = new WikiUrls() {
      public String feed() {
        return "feed";
      }
      public String page(final String name) {
        return "page";
      }
      public String pagesRoot() {
        return "root";
      }
      public String search() {
        return "search";
      }
      public String resource(String path) {
        return "favicon";
      }
    };
    new AtomFeedWriter(urls).writeAtom(changes, new PrintWriter(out));
    InputSource input = new InputSource(new StringReader(out.toString()));
    
    XPathContext feed = XPathContextFactory.newInstance().newXPathContext(input);
    feed.setNamespaceBindings(Collections.singletonMap("atom", AtomFeedWriter.ATOM_NS));
    
    assertEquals("feed", feed.evaluate("atom:feed/atom:link/@href", STRING));
    assertEquals(1.0, feed.evaluate("count(//atom:entry)", NUMBER));
    XPathContext entry = feed.evaluate("atom:feed/atom:entry", CONTEXT);
    assertEquals("page?revision=123", entry.evaluate("atom:id", STRING));
  }

}
