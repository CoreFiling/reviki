package net.hillsdon.svnwiki.wiki.renderer;

import java.lang.reflect.Array;
import java.util.regex.Matcher;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;

// Adapted from the Creole 0.4 implementation in JavaScript available here
// http://www.meatballsociety.org/Creole/0.4/
// Original copyright notice follows:

// Copyright (c) 2007 Chris Purcell.
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

/**
 * An incomplete Creole 1.0 renderer.
 * 
 * @see <a href="http://www.wikicreole.org/wiki/Creole1.0">The 1.0 specification.</a>
 * @author mth
 */
public class CreoleRenderer {

  public static class RawUrlNode extends AbstractRegexNode {
    public RawUrlNode() {
      super("\\b\\p{Alnum}{2,}:[^\\s\\[\\]\"'\\(\\)]{2,}[^\\s\\[\\]\"'\\(\\)\\,\\.]");
    }
    public String handle(final PageReference page, final Matcher matcher) {
      String escaped = Escape.html(matcher.group(0));
      return String.format("<a href='%s'>%s</a>", escaped, escaped);
    }
  }
  private static class Heading extends RegexMatchToTag {
    public Heading(final int number) {
      super(String.format("(?:^|\n)={%d}(.+?)(?:\n|$)", number), "h" + number, 1);
    }
  }
  private static class ListNode extends RegexMatchToTag {
    public ListNode(final String match, final String tag) {
      super("(^|\\n)(\\s*?" + match + "[^*#].*(\\n|$)(\\s*[*#]{2}.*(\\n|$))*)+", tag, 0, "(^|\\n)\\s*?[*#]", "$1");
    }
  }

  private final RenderNode _root;
  
  public CreoleRenderer(final RenderNode[] customNonStructural) {
    RenderNode root = new RegexMatchToTag("", "", 0);
    RenderNode noWiki = new RegexMatchToTag("(?s)(^|\\n)\\{\\{\\{(.*?)\\}\\}\\}(\\n|$)", "pre", 2);
    RenderNode paragraph = new RegexMatchToTag("(^|\\n)([ \\t]*[^\\s].*(\\n|$))+", "p", 0);
    RenderNode italic = new RegexMatchToTag("//(.*?)//", "em", 1);
    RenderNode strikethrough = new RegexMatchToTag("--(.+?)--", "del", 1);
    RenderNode bold = new RegexMatchToTag("[*][*](.*?)[*][*]", "strong", 1);
    RenderNode lineBreak = new RegexMatchToTag("\\\\\\\\", "br", null);
    RenderNode horizontalRule = new RegexMatchToTag("(^|\\n)\\s*----\\s*(\\n|$)", "hr", null);
    RenderNode unorderedList = new ListNode("\\*", "ul");
    RenderNode orderedList = new ListNode("#", "ol");
    RenderNode rawUrl = new RawUrlNode();
    RenderNode[] defaultNonStructural = {bold, italic, lineBreak, strikethrough, rawUrl};
    RenderNode[] nonStructural = concat(defaultNonStructural, customNonStructural);

    RenderNode table = new RegexMatchToTag("(^|\\n)(\\|.*\\|[ \\t]*(\\n|$))+", "table", 0);
    RenderNode tableRow = new RegexMatchToTag("(^|\\n)(\\|.*)\\|[ \\t]*(\\n|$)", "tr", 2);
    RenderNode tableHeading = new RegexMatchToTag("[|]+=([^|]*)", "th", 1);
    RenderNode tableCell = new RegexMatchToTag("[|]+([^|]*)", "td", 1);
    table.addChildren(tableRow);
    tableRow.addChildren(tableHeading, tableCell);
    tableCell.addChildren(concat(nonStructural, noWiki));
    
    RenderNode listItem = new RegexMatchToTag(".+(\\n[*#].+)*", "li", 0)
                              .addChildren(unorderedList, orderedList).addChildren(nonStructural);
    root.addChildren(
        noWiki.addChildren(), 
        horizontalRule,
        table,
        new Heading(5).addChildren(nonStructural),
        new Heading(4).addChildren(nonStructural), 
        new Heading(3).addChildren(nonStructural), 
        new Heading(2).addChildren(nonStructural), 
        new Heading(1).addChildren(nonStructural),
        orderedList.addChildren(listItem),
        unorderedList.addChildren(listItem),
        paragraph.addChildren(orderedList, unorderedList, noWiki, table, horizontalRule).addChildren(nonStructural), 
        italic.addChildren(nonStructural), 
        bold.addChildren(nonStructural),
        strikethrough.addChildren(nonStructural)
     );
    _root = root;
  }
  
  public String render(final PageReference page, final String in) {
    return _root.render(page, in.replaceAll("\r", ""));
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T[] concat(final T[] some, final T... more) {
    T[] all = (T[]) Array.newInstance(some.getClass().getComponentType(), some.length + more.length);
    System.arraycopy(some, 0, all, 0, some.length);
    System.arraycopy(more, 0, all, some.length, more.length);
    return all;
  }
  
}
