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
package net.hillsdon.reviki.webtests;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.AssertionFailedError;
import net.hillsdon.xhtmlvalidator.XHTMLValidator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowAdapter;
import com.gargoylesoftware.htmlunit.WebWindowEvent;

/**
 * Performs XHTML validation of the content.
 * 
 * @author mth
 */
class ValidateOnContentChange extends WebWindowAdapter {

  private final XHTMLValidator _validator = new XHTMLValidator();

  public void webWindowContentChanged(final WebWindowEvent event) {
    WebResponse response = event.getNewPage().getWebResponse();
    String content = response.getContentAsString();
    try {
      // We leave documents without a doctype alone for now.  These include
      // tomcat's default error pages.
      if (content.indexOf("<!DOCTYPE") != -1) {
        _validator.validate(new InputSource(new StringReader(content)));
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