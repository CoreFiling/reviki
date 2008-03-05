package net.hillsdon.svnwiki.wiki.renderer;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatchToTag {

  private final List<RegexMatchToTag> _children = new ArrayList<RegexMatchToTag>();
  private final Pattern _matchRe;
  private final String _tag;
  private final Integer _contentGroup;
  private final Pattern _replaceRe;
  private final String _replaceString;

  public RegexMatchToTag(final String matchRe, final String tag, final Integer contentGroup) {
    this(matchRe, tag, contentGroup, null, null);
  }

  public RegexMatchToTag(final String matchRe, final String tag, final Integer contentGroup, final String replaceRe, final String replaceString) {
    _matchRe = Pattern.compile(matchRe);
    _tag = tag;
    _contentGroup = contentGroup;
    _replaceRe = replaceRe == null ? null : Pattern.compile(replaceRe);
    _replaceString = replaceString;
  }

  public List<RegexMatchToTag> getChildren() {
    return _children;
  }

  public RegexMatchToTag setChildren(final RegexMatchToTag... rules) {
    _children.clear();
    _children.addAll(asList(rules));
    return this;
  }

  public String render(final String text) {
    if (text == null || text.length() == 0) {
      return "";
    }
    RegexMatchToTag earliestRule = null;
    Matcher earliestMatch = null;
    int earliestIndex = Integer.MAX_VALUE;
    for (RegexMatchToTag child : _children) {
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
      result += htmlEscape(text.substring(0, earliestMatch.start()));
      // Handle the match and recurse.
      result += earliestRule.handle(earliestRule, earliestMatch);
      result += render(text.substring(earliestMatch.end()));
      return result;
    }
    return htmlEscape(text);
  }

  private String handle(final RegexMatchToTag node, final Matcher matcher) {
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
    return _matchRe.matcher(text);
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
