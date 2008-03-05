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
  private final int _group;

  public RuleTreeNode(final String re, final String tag) {
    this(re, tag, 1);
  }
  
  public RuleTreeNode(final String re, final String tag, int group) {
    _group = group;
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
      return text;
    }
  }

  private String handle(final RuleTreeNode node, final Matcher matcher) {
    return "<" + _tag + ">" + node.render(matcher.group(_group)) + "</" + _tag + ">";
  }

  private Matcher matcher(final String text) {
    return _re.matcher(text);
  }

}
