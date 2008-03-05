package net.hillsdon.svnwiki.wiki.plugin;

import junit.framework.TestCase;

public class TestPluginClassLoader extends TestCase {

  private static final String EXAMPLE_PLUGIN_JAR = "test-plugin.jar";
  private static final String EXAMPLE_CLASS_NAME = "net.hillsdon.svnwiki.test.plugin.Example";

  public void test() throws Exception {
    PluginClassLoader classloader = new PluginClassLoader(getClass().getResource(EXAMPLE_PLUGIN_JAR), getClass().getClassLoader());
    Class<?> clazz = classloader.loadClass(EXAMPLE_CLASS_NAME);
    assertSame(classloader, clazz.getClassLoader());
  }
  
}
