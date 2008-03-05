package net.hillsdon.svnwiki.wiki.plugin;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestPluginClassLoader extends TestCase {

  private static final String EXAMPLE_PLUGIN_JAR = "test-plugin.jar";
  private static final String DIRECTLY_IN_JAR_CLASS_NAME = "net.hillsdon.svnwiki.test.plugin.Example";
  private static final String IN_EMBEDDED_JAR_CLASS_NAME = "net.hillsdon.svnwiki.test.plugin.Dependency";

  public void test() throws Exception {
    final PluginClassLoader classloader = new PluginClassLoader(getClass().getResource(EXAMPLE_PLUGIN_JAR), getClass().getClassLoader());
    final Class<?> exampleClass = classloader.loadClass(DIRECTLY_IN_JAR_CLASS_NAME);
    assertSame(classloader, exampleClass.getClassLoader());
    assertSame(classloader, classloader.loadClass(IN_EMBEDDED_JAR_CLASS_NAME).getClassLoader());
    
    List<Class<?>> expectedContributedClasses = new ArrayList<Class<?>>();
    expectedContributedClasses.add(exampleClass);
    assertEquals(expectedContributedClasses, classloader.getContributedClasses());
  }
  
}
