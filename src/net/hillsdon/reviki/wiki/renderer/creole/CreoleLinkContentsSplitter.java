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

public class CreoleLinkContentsSplitter implements LinkContentSplitter {

  public LinkParts split(final MatchResult match) {
    String in = match.group(1);
    // [[wiki:PageName|Show this text]]
    // [[http://somewhere|Show this text]], so we have an ambiguity here.
    int pipeIndex = in.lastIndexOf("|");
    int colonIndex = in.indexOf(":");
    
    String text = pipeIndex == -1 ? in : in.substring(pipeIndex + 1);
    String wiki = colonIndex == -1 ? null : in.substring(0, colonIndex);
    String refd = in.substring(colonIndex + 1, pipeIndex == -1 ? in.length() : pipeIndex);
    if (refd.startsWith("/")) {
      refd = wiki + ":" + refd;
      wiki = null;
    }
    return new LinkParts(text, wiki, refd);
  }

}
