package net.hillsdon.svnwiki.wiki.plugin;

import junit.framework.TestCase;

public class TestPluginClassLoader extends TestCase {

  private static final String EXAMPLE_PLUGIN_JAR = "test-plugin.jar";
  private static final String DIRECTLY_IN_JAR_CLASS_NAME = "net.hillsdon.svnwiki.test.plugin.Example";
  private static final String IN_EMBEDDED_JAR_CLASS_NAME = "net.hillsdon.svnwiki.test.plugin.Dependency";

  public void test() throws Exception {
    PluginClassLoader classloader = new PluginClassLoader(getClass().getResource(EXAMPLE_PLUGIN_JAR), getClass().getClassLoader());
    assertSame(classloader, classloader.loadClass(DIRECTLY_IN_JAR_CLASS_NAME).getClassLoader());
    assertSame(classloader, classloader.loadClass(IN_EMBEDDED_JAR_CLASS_NAME).getClassLoader());
  }
  
}
