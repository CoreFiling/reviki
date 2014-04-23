package net.hillsdon.reviki.webtests;

import java.io.IOException;

import junit.framework.AssertionFailedError;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.validation.SimpleDocumentValidator.SchemaReadException;
import nu.validator.xml.SystemErrErrorHandler;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An XHTML5 validator using vnu.jar from http://github.com/validator
 *
 * @author pwc
 */
public class XHTML5Validator {
  private SimpleDocumentValidator _validator = new SimpleDocumentValidator();
  private static String _schemaLoc = "http://s.validator.nu/html5-all.rnc";
  
  public XHTML5Validator() {
    setUpValidator();
  }

  private void setUpValidator() {
    try {
      // http://s.validator.nu/* schemas are retrieved from the local entity cache in vnu.jar
      _validator.setUpMainSchema(_schemaLoc, new SystemErrErrorHandler());
      _validator.setUpValidatorAndParsers(new ErrorHandler() {
        public void warning(SAXParseException e) throws SAXException {
          System.err.println(String.format("WARNING: Line %d. Col %d. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }
        
        public void fatalError(SAXParseException e) throws SAXException {
          throw new AssertionFailedError(String.format("FATAL ERROR: Line %d. Col %d. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }
        
        public void error(SAXParseException e) throws SAXException {
          throw new AssertionFailedError(String.format("ERROR: Line %d. Col %d. %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }
      }, true, true);
    }
    catch (SchemaReadException e) {
      throw new RuntimeException(String.format("Failed to read schema from local entity cache: %s", _schemaLoc));
    }
    catch (Exception e) {
      // test setup failed
      throw new RuntimeException(String.format("XHTML5 test validation failed: %s", e.getMessage()));
    }
  }
  
  public void validate(final InputSource inputSource) throws IOException {
    try {
      _validator.checkXmlInputSource(inputSource);
    }
    catch (SAXException e) {
      // handled by ErrorHandler on the validator
    }
  }
}
