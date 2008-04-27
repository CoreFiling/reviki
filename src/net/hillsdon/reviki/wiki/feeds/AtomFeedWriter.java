package net.hillsdon.reviki.wiki.feeds;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class AtomFeedWriter implements FeedWriter {

  public static final String ATOM_NS = "http://www.w3.org/2005/Atom";
  
  private static final Attributes NO_ATTRIBUTES = new AttributesImpl();

  private void addElement(final ContentHandler handler, final String name, final String content) throws SAXException {
    handler.startElement(ATOM_NS, name, name, NO_ATTRIBUTES);
    handler.characters(content.toCharArray(), 0, content.length());
    handler.endElement(ATOM_NS, name, name);
  }

  private void addLink(final ContentHandler handler, final String href, final String rel) throws SAXException {
    AttributesImpl attrs = new AttributesImpl();
    attrs.addAttribute("", "href", "href", "CDATA", href);
    if (rel != null) {
      attrs.addAttribute("", "rel", "rel", "CDATA", rel);
    }
    handler.startElement(ATOM_NS, "link", "link", attrs);
    handler.endElement(ATOM_NS, "link", "link");
  }

  private void addUpdated(final ContentHandler handler, Date date) throws SAXException {
    addElement(handler, "updated", rfc3339DateFormat(date));
  }

  private String rfc3339DateFormat(final Date date) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat.format(date);
  }

  public void writeAtom(final WikiUrls wikiUrls, final PrintWriter out, final Collection<ChangeInfo> changes) throws TransformerConfigurationException, SAXException {
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
        
        handler.startElement(ATOM_NS, "author", "author", NO_ATTRIBUTES);
        addElement(handler, "name", change.getUser());
        handler.endElement(ATOM_NS, "author", "author");
        
        addElement(handler, "summary", change.getDescription());
        addUpdated(handler, change.getDate());
        handler.endElement(ATOM_NS, "entry", "entry");
      }
    }
    handler.endElement(ATOM_NS, "feed", "feed");
    handler.endDocument();
  }

}
