/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.wiki.renderer;

import junit.framework.TestCase;

public class TestCustomWikiLinkNode extends TestCase {

  public void test() {
    CustomWikiLinkNode node = new CustomWikiLinkNode(null);
    // We used to reject the 'See' and never consider the following text.
    assertNotNull(node.find("\nSee ConfigInterWikiLinks (ugly at present) for configuration."));
  }
  
}
