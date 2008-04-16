package net.hillsdon.reviki.vc.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.AutoProperties;

public class TestAutoPropertiesApplierImpl extends TestCase {

  private AutoPropertiesApplierImpl _applier;
  private Map<String, String> _autoproperties = new LinkedHashMap<String, String>();

  @Override
  protected void setUp() throws Exception {
    final AutoProperties autoproperties = new AutoProperties() {
      public Map<String, String> read() {
        return _autoproperties;
      }
    };
    _applier = new AutoPropertiesApplierImpl(autoproperties);
  }
  
  public void test() throws Exception {
    _autoproperties.put("*.bat", "svn:mime-type=text/plain;svn:eol-style=CRLF");
    Map<String, String> expected = new LinkedHashMap<String, String>();
    expected.put("svn:mime-type", "text/plain");
    expected.put("svn:eol-style", "CRLF");
    assertEquals(Collections.emptyMap(), _applier.apply("foo.bat"));
    _applier.read();
    assertEquals(expected, _applier.apply("foo.bat"));
  }
  
}
