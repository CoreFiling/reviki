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
package net.hillsdon.reviki.wiki.renderer;

import junit.framework.TestCase;

public class TestCustomWikiLinkNode extends TestCase {

  private CustomWikiLinkNode _node;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _node = new CustomWikiLinkNode(null);
  }

  public void test() {
    // We used to reject the 'See' and never consider the following text.
    assertNotNull(_node.find("\nSee ConfigInterWikiLinks (ugly at present) for configuration."));
  }

  public void testNoMatch() {
    assertNull(_node.find("Some text without link"));
  }

  public void testMatch() {
    assertEquals("WikiLink", _node.find("Some text with a WikiLink").group());
  }

  public void testMatchDot() {
    assertEquals("Link1.0", _node.find("Some text with a Link1.0").group());
  }

  public void testMatchInterWikiDot() {
    assertEquals("wiki:Link1.0", _node.find("Some text with a wiki:Link1.0").group());
  }

  public void testMatchInterWikiMultipleDots() {
    assertEquals("wiki:Link1.0.0", _node.find("Some text with a wiki:Link1.0.0").group());
  }

  public void testMatchInterWikiMultipleAdjacentDots() {
    assertEquals("wiki:Link1..0", _node.find("Some text with a wiki:Link1..0").group());
  }

  public void testMatchInterWikiMultipleAdjacentDotsEnding() {
    assertEquals("wiki:Link1", _node.find("Some text with a wiki:Link1..").group());
  }

}
