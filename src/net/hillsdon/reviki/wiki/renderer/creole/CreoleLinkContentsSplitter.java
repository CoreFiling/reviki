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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CreoleLinkContentsSplitter {
  /**
   * Splits links where target is
   * 
   * PageName wiki:PageName PageName#fragment wiki:PageName#fragment
   * A String representing an absolute URI scheme://valid/absolute/uri
   * Any character not in the `unreserved`, `punct`, `escaped`, or `other` categories (RFC 2396),
   * and not equal '/' or '@', is %-encoded. 
   * 
   * @param in The String to split
   * @return The split LinkParts
   */
  public static LinkParts split(String target, String text) {
    if (target == null) {
      target = "";
    }
    if (text == null) {
      text = target;
    }
    // Link target can be PageName, wiki:PageName or a URL.
    URI uri = null;
    try {
      try {
        uri = new URI(target);
      }
      catch (URISyntaxException e) {
        // The URI class is a bit stricter at parsing than we'd really like to be
        URL url = new URL(target);
        uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
      }
      if (uri.getPath()==null || !uri.isAbsolute()) {
        uri = null;
      }
    }
    catch (URISyntaxException e) {
      // uri remains null
    }
  catch (MalformedURLException e) {
     // uri remains null
  }

    if (uri != null) {
      return new LinkParts(text, uri);
    }
    else {
      // Split into wiki:pageName
      String[] parts = target.split(":", 2);
      String wiki = null;
      String pageName = target;
      if (parts.length == 2) {
        wiki = parts[0];
        pageName = parts[1];
      }

      // Split into pageName#fragment
      parts = pageName.split("#", 2);
      String fragment = null;
      if (parts.length == 2) {
        pageName = parts[0];
        fragment = parts[1];
      }

      // Split into pageName?revision
      parts = pageName.split("\\?revision=", 2);
      String revision = null;
      if (parts.length == 2) {
        pageName = parts[0];
        revision = parts[1];
      }

      // Split into pageName/attachment
      parts = pageName.split("/", 2);
      String attachment = null;
      if (parts.length == 2) {
        pageName = parts[0];
        attachment = parts[1];
      }

      return new LinkParts(text, wiki, pageName, revision, fragment, attachment);
    }
  }

}
