package net.hillsdon.reviki.webtests;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.AssertionFailedError;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowAdapter;
import com.gargoylesoftware.htmlunit.WebWindowEvent;

/**
 * Performs XHTML validation of the content.
 * 
 * @author mth
 */
class ValidateOnContentChange extends WebWindowAdapter {

  private final XMLReader _reader;

  public ValidateOnContentChange() {
    try {
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      SAXParser parser;
      parser = factory.newSAXParser();
      _reader = parser.getXMLReader();
      _reader.setErrorHandler(new ErrorHandler() {
        public void error(SAXParseException e) throws SAXException {
          throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
          throw e;
        }

        public void warning(SAXParseException e) throws SAXException {
          throw e;
        }
      });
      final CatalogResolver cr = new CatalogResolver(true);
      cr.getCatalog().getCatalogManager().setIgnoreMissingProperties(true);
      cr.getCatalog().parseCatalog(ValidateOnContentChange.class.getResource("resources/catalog.xml"));
      _reader.setEntityResolver(cr);
    }
    catch (Exception ex) {
      throw new RuntimeException("Parser configuration error", ex);
    }
  }

  private void validate(final Reader in) throws SAXException, IOException {
    _reader.parse(new InputSource(in));
  }

  public void webWindowContentChanged(final WebWindowEvent event) {
    WebResponse response = event.getNewPage().getWebResponse();
    String content = response.getContentAsString();
    try {
      // We leave documents without a doctype alone for now.  These include
      // tomcat's default error pages.
      if (content.indexOf("<!DOCTYPE") != -1) {
        validate(new StringReader(content));
      }
    }
    catch (SAXException e) {
      System.err.println("\n XHTML validation error: " + e.getMessage() + "\n\n");
      System.err.println(content);
      throw new AssertionFailedError("XHTML validation error, see console output.");
    }
    catch (IOException e) {
      throw new RuntimeException("I/O error reading from a String!", e);
    }
  }

}