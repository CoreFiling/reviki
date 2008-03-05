package net.hillsdon.svnwiki.wiki.renderer;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleTreeNode {

  private final List<RuleTreeNode> _children = new ArrayList<RuleTreeNode>();
  private final Pattern _re;
  private final String _tag;
  private final Integer _contentGroup;
  private final Pattern _replaceRe;
  private final String _replaceString;

  public RuleTreeNode(final String re, final String tag, final Integer contentGroup) {
    this(re, tag, contentGroup, null, null);
  }

  public RuleTreeNode(final String re, final String tag, final Integer contentGroup, final String replaceRe, final String replaceString) {
    _contentGroup = contentGroup;
    _replaceRe = replaceRe == null ? null : Pattern.compile(replaceRe);
    _replaceString = replaceString;
    _re = Pattern.compile(re);
    _tag = tag;
  }

  public List<RuleTreeNode> getChildren() {
    return _children;
  }

  public RuleTreeNode setChildren(final RuleTreeNode... rules) {
    _children.clear();
    _children.addAll(asList(rules));
    return this;
  }

  public String render(final String text) {
    if (text == null || text.length() == 0) {
      return "";
    }
    RuleTreeNode earliestRule = null;
    Matcher earliestMatch = null;
    int earliestIndex = Integer.MAX_VALUE;
    for (RuleTreeNode child : _children) {
      Matcher matcher = child.matcher(text);
      if (matcher.find()) {
        if (matcher.start() < earliestIndex) {
          earliestIndex = matcher.start();
          earliestMatch = matcher;
          earliestRule = child;
        }
      }
    }
    if (earliestRule != null) {
      String result = "";
      // Just output the stuff before the match.
      result += text.substring(0, earliestMatch.start());
      // Handle the match and recurse.
      result += earliestRule.handle(earliestRule, earliestMatch);
      result += render(text.substring(earliestMatch.end()));
      return result;
    }
    else {
      return htmlEscape(text);
    }
  }

  private String handle(final RuleTreeNode node, final Matcher matcher) {
    if (_contentGroup == null) {
      return "<" + _tag + " />";
    }
    String text = matcher.group(_contentGroup);
    if (_replaceRe != null) {
      text = _replaceRe.matcher(text).replaceAll(_replaceString);
    }
    return "<" + _tag + ">" +  node.render(text) + "</" + _tag + ">";
  }

  private Matcher matcher(final String text) {
    return _re.matcher(text);
  }
  
  /**
   * HTML escaping routine.
   * @param content the unescaped content.
   * @return the escaped output.
   */
  private static String htmlEscape(final String content) {
    char[] chars = content.toCharArray();
    final StringBuffer result = new StringBuffer(2 * chars.length);
    for (int i = 0; i < chars.length; ++i) {
      char character = chars[i];
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else if (character == '&') {
        result.append("&amp;");
     }
      else if (character == '\"') {
        result.append("&quot;");
      }
      else if (character == '\'') {
        result.append("&#039;");
      }
      else if (character == '\\') {
         result.append("&#092;");
      }
      else {
        result.append(character);
      }
    }
    return result.toString();
  }
  
}
