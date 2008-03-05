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
package net.hillsdon.svnwiki.wiki;

import static java.lang.String.format;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.text.WikiWordUtils;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class InternalLinker {

  private final PageStore _store;
  private final String _wikiName;
  private final String _contextPath;

  public InternalLinker(final String contextPath, final String wikiName, final PageStore store) {
    _contextPath = contextPath;
    _wikiName = wikiName;
    _store = store;
  }

  private boolean exists(final String name) {
    try {
      return _store.list().contains(new PageReference(name));
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String url(final String pageName) {
    return String.format("%s/pages/%s%s", 
      _contextPath,
      _wikiName != null ? Escape.url(_wikiName) + "/" : "", 
      Escape.url(pageName)
    );
  }
  
  public String link(final String pageName, final String linkText) {
    boolean exists = exists(pageName);
    if (!exists && WikiWordUtils.isAcronym(pageName)) {
      return Escape.html(pageName);
    }
    String cssClass = exists ? "existing-page" : "new-page";
    return format("<a class='%s' href='%s'>%s</a>",
      cssClass,
      Escape.html(url(pageName)),
      Escape.html(linkText)
    );
  }
  
}
