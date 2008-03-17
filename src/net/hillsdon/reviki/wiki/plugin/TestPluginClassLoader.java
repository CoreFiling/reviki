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
package net.hillsdon.reviki.wiki.plugin;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestPluginClassLoader extends TestCase {

  private static final String EXAMPLE_PLUGIN_JAR = "test-plugin.jar";
  private static final String DIRECTLY_IN_JAR_CLASS_NAME = "net.hillsdon.reviki.test.plugin.Example";
  private static final String IN_EMBEDDED_JAR_CLASS_NAME = "net.hillsdon.reviki.test.plugin.Dependency";

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
