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
package net.hillsdon.reviki.wiki.renderer;

import static net.hillsdon.reviki.text.WikiWordUtils.isWikiWord;

import java.util.regex.Matcher;

import net.hillsdon.reviki.wiki.renderer.creole.LinkNode;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

/**
 * Links like c2:WikiPage and WikiPage but very generous as to
 * what counts as a wiki word.
 * 
 * @author mth
 */
public class CustomWikiLinkNode extends LinkNode {
  
  public CustomWikiLinkNode(final LinkPartsHandler handler) {
    super("(\\p{Alnum}+:)?(\\p{Alnum}+)", new CustomLinkContentSplitter(), handler);
  }

  @Override
  protected boolean confirmMatchFind(final Matcher matcher) {
    do {
      String wikiName = matcher.group(1);
      String pageName = matcher.group(2);
      if (wikiName != null || isWikiWord(pageName)) {
        return true;
      }
    } while (matcher.find());
    return false;
  }

}
