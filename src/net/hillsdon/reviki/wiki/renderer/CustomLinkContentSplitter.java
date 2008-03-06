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

import java.util.regex.MatchResult;

import net.hillsdon.reviki.wiki.renderer.creole.LinkContentSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;

public class CustomLinkContentSplitter implements LinkContentSplitter {

  public LinkParts split(final MatchResult in) {
    String wikiName = in.group(1);
    String pageName = in.group(2);
    if (wikiName != null) {
      wikiName = wikiName.substring(0, wikiName.length() - 1);
    }
    return new LinkParts(in.group(), wikiName, pageName);
  }

}
