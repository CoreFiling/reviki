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

import java.util.regex.MatchResult;

import net.hillsdon.fij.text.Strings;

import org.apache.commons.lang.StringUtils;

public class CreoleLinkContentsSplitter implements LinkContentSplitter {

  public LinkParts split(final MatchResult match) {
    String in = match.group(1);
    return split(in);
  }

  LinkParts split(final String in) {
    String link = StringUtils.trimToNull(StringUtils.substringBefore(in, "|"));
    String text = StringUtils.trimToNull(StringUtils.substringAfter(in, "|"));
    if (text == null) {
      text = link;
    }
    String[] parts = link == null ? new String[] {""} : StringUtils.split(link, ":", 2);
    String wiki = null;
    // Link can be PageName, wiki:PageName or a URL.  Assume URL based on ':/'.
    if (parts.length == 2 && !"/".equals(Strings.sCharAt(parts[1], 0))) {
      wiki = parts[0];
      link = parts[1];
    }
    return new LinkParts(text, wiki, link);
  }

}
