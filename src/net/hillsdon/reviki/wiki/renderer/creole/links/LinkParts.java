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
package net.hillsdon.reviki.wiki.renderer.creole.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import net.hillsdon.reviki.web.urls.UnknownWikiException;

public class LinkParts {
  private final String _text;

  private LinkTarget _target;
  
  public LinkParts(final String text, final String wiki, String pageName, final String fragment, String attachment) {
    _text = text;
    
    if (attachment != null) {
      _target = new AttachmentLinkTarget(wiki, "attachments".equals(pageName) ? null : pageName, attachment);
    }
    else {
      _target = new PageLinkTarget(wiki, pageName, fragment);
    }
  }

  public LinkParts(final String text, final URI uri) {
    _text = text;
    _target = new ExternalLinkTarget(uri);
  }

  public String getText() {
    return _text;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + Arrays.asList(_text, _target).toString();
  }
  
  public boolean exists(LinkResolutionContext linkResolutionContext) {
    return _target.exists(linkResolutionContext);
  }

  public boolean isURL() {
    return _target.isURL();
  }

  public String getStyleClass(LinkResolutionContext linkResolutionContext) {
    return _target.getStyleClass(linkResolutionContext);
  }

  public String getURL(LinkResolutionContext linkResolutionContext) throws URISyntaxException, UnknownWikiException {
    return _target.getURL(linkResolutionContext);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_target == null) ? 0 : _target.hashCode());
    result = prime * result + ((_text == null) ? 0 : _text.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LinkParts other = (LinkParts) obj;
    if (_target == null) {
      if (other._target != null)
        return false;
    }
    else if (!_target.equals(other._target))
      return false;
    if (_text == null) {
      if (other._text != null)
        return false;
    }
    else if (!_text.equals(other._text))
      return false;
    return true;
  }

  public boolean isNoFollow(LinkResolutionContext resolver) {
    return _target.isNoFollow(resolver);
  }

}