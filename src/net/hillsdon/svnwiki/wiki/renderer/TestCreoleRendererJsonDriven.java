package net.hillsdon.svnwiki.wiki.renderer;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageReference;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.JavaTypeMapper;

public class TestCreoleRendererJsonDriven extends TestCase {

  private List<Map<String, String>> _tests;
  private CreoleRenderer _renderer;

  @SuppressWarnings("unchecked")
  public TestCreoleRendererJsonDriven() throws Exception {
    JsonFactory jf = new JsonFactory();
    _tests = (List<Map<String, String>>) new JavaTypeMapper().read(jf.createJsonParser(getClass().getResource("unit-test-data.json")));
    _renderer = new CreoleRenderer(new RenderNode[0]);
  }
  
  public void test() throws Exception {
    int errors = 0;
    for (Map<String, String> test : _tests) {
      final String caseName = test.get("name");
      final String expected = test.get("output");
      final String input = test.get("input");
      final String actual = _renderer.render(new PageReference(""), input);
      final boolean match = expected.equals(actual);
      if (test.get("bug") != null) {
        assertFalse("You fixed " + caseName, match);
        continue;
      }
      if (!match) {
        errors++;
        System.err.println("Creole case: " + caseName);
        System.err.println("Input:\n" + input);
        System.err.println("Expected:\n" + expected);
        System.err.println("Actual:\n" + actual);
        System.err.println();
      }
    }
    if (errors > 0) {
      fail("Creole rendering errors, please see stderr.");
    }
  }
  
}
