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
