/**
 * Copyright 2007 Matthew Hillsdon
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
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JavaTypeMapper;

public abstract class JsonDrivenRenderingTest extends TestCase {

  private List<Map<String, String>> _tests;

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

  protected abstract String render(String input) throws Exception;
  
}
