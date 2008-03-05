package net.hillsdon.svnwiki.wiki.renderer;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageReference;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.JavaTypeMapper;

public class TestCreoleRendererExternallyDefinedTests extends TestCase {

  private List<Map<String, String>> _tests;
  private CreoleRenderer _renderer;

  @SuppressWarnings("unchecked")
  public TestCreoleRendererExternallyDefinedTests() throws Exception {
    JsonFactory jf = new JsonFactory();
    _tests = (List<Map<String, String>>) new JavaTypeMapper().read(jf.createJsonParser(getClass().getResource("unit-test-data.json")));
    _renderer = new CreoleRenderer(new RenderNode[0]);
  }
  
  public void test() throws Exception {
    for (Map<String, String> test : _tests) {
      assertEquals(test.get("output"), _renderer.render(new PageReference(""), test.get("input")));
    }
  }
  
}
