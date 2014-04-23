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
package net.hillsdon.reviki.wiki;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RenderedPage {

  private static final Pattern RE_NEW_PAGE_CLASS = Pattern.compile("(^|\\s)new-page($|\\s)");
  private static final Pattern RE_EXIST_PAGE_CLASS = Pattern.compile("(^|\\s)existing-page($|\\s)");
  
  private final String _pageName;
  private final String _rendered; 

  public RenderedPage(final String pageName, final ResultNode resultNode) {
    _pageName = pageName;
    _rendered = resultNode.toXHTML();
  }

  public String getPage() {
    return _pageName;
  }
  
  /**
   * @return outgoing links in document order.
   * @throws IOException If we fail to parse. 
   */
  public List<String> findOutgoingWikiLinks() throws IOException {
    final List<String> outgoing = new ArrayList<String>();
    SAXParser parser = new SAXParser();
    parser.setContentHandler(new DefaultHandler() {
      public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
        if (localName.equals("A")) {
          boolean wikiPageClass = false;
          String href = null;
          for (int i = 0, len = attributes.getLength(); i < len; ++i) {
            if ("class".equals(attributes.getLocalName(i))) {
              wikiPageClass = hasWikiPageClass(attributes.getValue(i));
            }
            else if ("href".equals(attributes.getLocalName(i))) {
              href = attributes.getValue(i);
            }
          }
          if (wikiPageClass && href != null) {
            int lastSlash = href.lastIndexOf('/');
            outgoing.add(href.substring(lastSlash + 1));
          }
        }
      }
    });
    try {
      parser.parse(new InputSource(new StringReader(_rendered)));
    }
    catch (final SAXException ex) {
      throw new IOException("Parse error") {
        private static final long serialVersionUID = 1L;
        @Override
        public Throwable getCause() {
          return ex;
        }
      };
    }
    return outgoing;
  }

  private boolean hasWikiPageClass(final String clazz) {
    return RE_EXIST_PAGE_CLASS.matcher(clazz).find() || RE_NEW_PAGE_CLASS.matcher(clazz).find();
  }

}
