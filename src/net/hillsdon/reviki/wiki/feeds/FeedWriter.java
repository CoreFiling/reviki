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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.hillsdon.reviki.text.WikiWordUtils;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.StoreKind;
import net.hillsdon.reviki.wiki.WikiUrls;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts changes to syndication formats. 
 * 
 * @author mth
 */
public class FeedWriter {

  public static final String ATOM_NS = "http://www.w3.org/2005/Atom";
  
  private static final Attributes NO_ATTRIBUTES = new AttributesImpl();

  private static void addElement(final TransformerHandler handler, final String name, final String content) throws SAXException {
    handler.startElement(ATOM_NS, name, name, NO_ATTRIBUTES);
    handler.characters(content.toCharArray(), 0, content.length());
    handler.endElement(ATOM_NS, name, name);
  }

  private static void addLink(final TransformerHandler handler, final String href, final String rel) throws SAXException {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute("", "href", "href", "CDATA", href);
    if (rel != null) {
      attrs.addAttribute("", "rel", "rel", "CDATA", rel);
    }
    handler.startElement(ATOM_NS, "link", "link", attrs);
    handler.endElement(ATOM_NS, "link", "link");
  }

  private static void addUpdated(TransformerHandler handler, Date date) throws SAXException {
    addElement(handler, "updated", rfc3339DateFormat(date));
  }

  private static String rfc3339DateFormat(final Date date) {
    return new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ").format(date);
  }

  public static void writeAtom(final WikiUrls wikiUrls, final PrintWriter out, final Collection<ChangeInfo> changes) throws TransformerConfigurationException, SAXException {
    StreamResult streamResult = new StreamResult(out);
    SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    
    TransformerHandler handler = tf.newTransformerHandler();
    Transformer serializer = handler.getTransformer();
    try {
      serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    }
    catch (IllegalArgumentException ex) {
      // Oh well, ugly XML then.
    }
    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      
    handler.setResult(streamResult);
    handler.startDocument();
    handler.startPrefixMapping("", ATOM_NS);
    handler.endPrefixMapping("");
    handler.startElement(ATOM_NS, "feed", "feed", NO_ATTRIBUTES);
    addElement(handler, "id", wikiUrls.feed());
    addElement(handler, "title", "reviki feed");
    addLink(handler, wikiUrls.feed(), "self");
    addLink(handler, wikiUrls.root(), null);
    addUpdated(handler, changes.isEmpty() ? new Date(0) : changes.iterator().next().getDate());
    for (ChangeInfo change : changes) {
      // For now.
      if (change.getKind() == StoreKind.PAGE) {
        handler.startElement(ATOM_NS, "entry", "entry", NO_ATTRIBUTES);
        addElement(handler, "title", WikiWordUtils.pathToTitle(change.getPage()));
        addLink(handler, wikiUrls.page(change.getPage()), null);
        addElement(handler, "id", wikiUrls.page(change.getPage()));
        addElement(handler, "author", change.getUser());
        addElement(handler, "summary", change.getDescription());
        addUpdated(handler, change.getDate());
        handler.endElement(ATOM_NS, "entry", "entry");
      }
    }
    handler.endElement(ATOM_NS, "feed", "feed");
    handler.endDocument();
  }

}
