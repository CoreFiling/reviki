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
package net.hillsdon.reviki.wiki.renderer.creole;

public class LinkParts {
  private final String _text;
  private final String _wiki;
  private final String _refd;
  public LinkParts(final String text, final String wiki, final String refd) {
    _text = text;
    _wiki = wiki;
    _refd = refd;
  }
  public String getText() {
    return _text;
  }
  public String getWiki() {
    return _wiki;
  }
  public String getRefd() {
    return _refd;
  }
  public boolean isURL() {
    return _wiki == null && getRefd().matches("\\p{L}+?:.*");
  }
}