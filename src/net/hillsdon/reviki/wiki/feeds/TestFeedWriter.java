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
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.StoreKind;
import net.hillsdon.reviki.wiki.WikiUrls;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
      public String root() {
        return "root";
      }
      public String search() {
        return "search";
      }
      public String favicon() {
        return "favicon";
      }
    };
    FeedWriter.writeAtom(urls, new PrintWriter(out), changes);
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document dom = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(out.toString())));
    Element selfLink = (Element) dom.getElementsByTagName("link").item(0);
    assertEquals(selfLink.getAttributeNS(null, "href"), "feed");
    
    NodeList entries = dom.getElementsByTagNameNS(FeedWriter.ATOM_NS, "entry");
    assertEquals(1, entries.getLength());
    // TODO, actually assert something useful.
  }
  
}
