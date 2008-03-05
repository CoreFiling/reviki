package net.hillsdon.svnwiki.wiki.plugin;

import junit.framework.TestCase;

public class TestPluginClassLoader extends TestCase {

  public void test() throws Exception {
    PluginClassLoader classloader = new PluginClassLoader(getClass().getResource("test-plugin.jar"), getClass().getClassLoader());
    Class<?> clazz = classloader.loadClass("net.hillsdon.svnwiki.test.plugin.Example");
    assertSame(classloader, clazz.getClassLoader());
  }
  
}
