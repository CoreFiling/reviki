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
package net.hillsdon.reviki.configuration;

import junit.framework.TestCase;

public class TestInitialConfiguration extends TestCase {

  public void testThrowsIllegalArgumentExceptionOnRubbishInput() {
    PropertiesDeploymentConfiguration configuration = new PropertiesDeploymentConfiguration();
    PerWikiInitialConfiguration perWikiConfiguration = new PerWikiInitialConfiguration(configuration, "SomeWiki", "SomeWiki");
    try {
      perWikiConfiguration.setUrl("foo bar");
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
  }
  
//// This touches the file system but is nonetheless useful from time to time.
//  public void testLoadSave() {
//    Configuration c1 = new Configuration();
//    c1.setUrl("http://localhost/svn/wiki");
//    c1.save();
//
//    Configuration c2 = new Configuration();
//    c2.load();
//    assertEquals("http://localhost/svn/wiki", c2.getUrl().toDecodedString());
//  }
  
}
