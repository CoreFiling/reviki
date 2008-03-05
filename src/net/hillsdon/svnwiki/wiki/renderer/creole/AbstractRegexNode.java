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
package net.hillsdon.svnwiki.wiki.renderer.creole;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;

public abstract class AbstractRegexNode implements RenderNode {

  private final List<RenderNode> _children = new ArrayList<RenderNode>();
  private final Pattern _matchRe;

  public AbstractRegexNode(final String matchRe) {
    _matchRe = Pattern.compile(matchRe);
  }

  public List<RenderNode> getChildren() {
    return _children;
  }

  public AbstractRegexNode addChildren(final RenderNode... rules) {
    _children.addAll(asList(rules));
    return this;
  }

  public String render(final PageReference page, final String text, final RenderNode parent) {
    if (text == null || text.length() == 0) {
      return "";
    }
    RenderNode earliestRule = null;
    Matcher earliestMatch = null;
    int earliestIndex = Integer.MAX_VALUE;
    for (RenderNode child : _children) {
      Matcher matcher = child.find(text);
      if (matcher != null && matcher.group(0).length() > 0) {
        if (matcher.start() < earliestIndex) {
          earliestIndex = matcher.start();
          earliestMatch = matcher;
          earliestRule = child;
        }
      }
    }
    if (earliestRule != null) {
      String beforeMatch = text.substring(0, earliestMatch.start());
      String afterMatch = text.substring(earliestMatch.end());
      String result = "";
      result += Escape.html(beforeMatch);
      //System.err.println("Matched by " + earliestRule + ": " + earliestMatch.group(0));
      result += earliestRule.handle(page, earliestMatch, this);
      result += render(page, afterMatch, parent);
      return result;
    }
    //System.err.println("No match using " + _children + ", test:\n" + text);
    return Escape.html(text);
  }

  public Matcher find(final String text) {
    Matcher matcher = _matchRe.matcher(text);
    return matcher.find() && confirmMatchFind(matcher) ? matcher : null;
  }

  protected boolean confirmMatchFind(final Matcher matcher) {
    return true;
  }

}
