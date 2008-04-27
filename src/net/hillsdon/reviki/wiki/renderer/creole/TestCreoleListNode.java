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
package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.regex.Matcher;

import junit.framework.TestCase;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer.ListNode;

/**
 * This one is a bit magical.
 * 
 * @author mth
 */
public class TestCreoleListNode extends TestCase {

  public void test() {
    ListNode node = new ListNode("[*]", "ul");
    Matcher found = node.find("* Foo\n* Bar\n\n* Blort\n");
    // We previously didn't break on the blank line.
    assertEquals("* Foo\n* Bar\n", found.group());
  }
  
}
