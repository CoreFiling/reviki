package net.hillsdon.svnwiki.wiki.renderer;

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
