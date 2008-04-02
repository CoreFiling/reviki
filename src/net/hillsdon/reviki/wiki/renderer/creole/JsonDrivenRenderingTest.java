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

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.xhtmlvalidator.XHTMLValidator;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JavaTypeMapper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class JsonDrivenRenderingTest extends TestCase {

  private static final String XHTML_PREFIX = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
                                           + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>XHTML prefix</title></head><body>";
  private static final String XHTML_SUFFIX = "</body></html>";
  private final XHTMLValidator _validator = new XHTMLValidator();
  private final List<Map<String, String>> _tests;

  @SuppressWarnings("unchecked")
  public JsonDrivenRenderingTest(final URL url) throws JsonParseException, IOException {
    JsonFactory jf = new JsonFactory();
    _tests = (List<Map<String, String>>) new JavaTypeMapper().read(jf.createJsonParser(url));
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
      validate(caseName, actual);
      final boolean match = expected.equals(actual);
      if (bugExplanation != null) {
        assertFalse("You fixed " + caseName, match);
        continue;
      }
      if (!match) {
        errors++;
        err.println("Creole case: " + caseName);
        err.println("Input:\n" + input);
        err.println("Expected:\n" + expected);
        err.println("Actual:\n" + actual);
        err.println();
      }
    }
    if (errors > 0) {
      fail("Rendering errors, please see stderr.");
    }
  }

  private void validate(final String caseName, final String actual) {
    // Put the content in a <body> tag first.
    final String content = XHTML_PREFIX + actual + XHTML_SUFFIX;
    try {
      _validator.validate(new InputSource(new StringReader(content)));
    }
    catch (SAXException e) {
      fail(caseName + ": " + e.getMessage());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract String render(String input) throws Exception;
  
}
