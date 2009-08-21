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
import java.util.List;

import junit.framework.AssertionFailedError;
import net.hillsdon.xhtmlvalidator.XHTMLValidator;

import org.jaxen.JaxenException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowAdapter;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
      if (!event.getWebWindow().getWebClient().isCookiesEnabled()) {
        final Page page = event.getNewPage();
        if (page instanceof HtmlPage) {
          final HtmlPage htmlPage = (HtmlPage) page;
          @SuppressWarnings("unchecked")
          final List<HtmlAnchor> anchors = (List<HtmlAnchor>) htmlPage.getByXPath("//a[@class!='inter-wiki' and @class!='external']");
          for(final HtmlAnchor a : anchors) {
            if (!a.getHrefAttribute().contains(";jsessionid=")) {
              final String message = 
                "Found a link without a jsessionid!\n" + 
                "Page Title: \t" + htmlPage.getTitleText() + "\n" + 
                "Line: \t" + a.getStartLineNumber() + "\n" + 
                "Link: \t" + a.asXml() + ".";
              throw new AssertionFailedError(message);
              
            }
          }
          @SuppressWarnings("unchecked")
          final List<HtmlForm> forms = (List<HtmlForm>) htmlPage.getByXPath("//form");
          for(final HtmlForm f : forms) {
            if (!f.getActionAttribute().contains(";jsessionid=")) {
              final String message = 
                "Found a form without a jsessionid!\n" + 
                "Page Title: \t" + htmlPage.getTitleText() + "\n" + 
                "Line: \t" + f.getStartLineNumber() + "\n" + 
                "Link: \t" + f.asXml() + ".";
              throw new AssertionFailedError(message);
              
            }
          }
        }
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
    catch (JaxenException e) {
      throw new AssertionFailedError("XHTML validation error, see console output.");
    }
  }

}