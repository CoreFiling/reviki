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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hillsdon.svnwiki.vc.PageReference;

public class RegexMatchToTag extends AbstractRegexNode implements RenderNode {

  private final String _tag;
  private final Integer _contentGroup;
  private final Pattern _replaceRe;
  private final String _replaceString;
  
  public RegexMatchToTag(final String matchRe, final String tag, final Integer contentGroup) {
    this(matchRe, tag, contentGroup, null, null);
  }

  public RegexMatchToTag(final String matchRe, final String tag, final Integer contentGroup, final String replaceRe, final String replaceString) {
    super(matchRe);
    _tag = tag;
    _contentGroup = contentGroup;
    _replaceRe = replaceRe == null ? null : Pattern.compile(replaceRe);
    _replaceString = replaceString;
  }

  public String handle(final PageReference page, final Matcher matcher) {
    if (_contentGroup == null) {
      return "<" + _tag + " />";
    }
    String text = matcher.group(_contentGroup);
    if (_replaceRe != null) {
      text = _replaceRe.matcher(text).replaceAll(_replaceString);
    }
    return "<" + _tag + ">" +  render(page, text) + "</" + _tag + ">";
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _tag + ">";
  }
  
}
