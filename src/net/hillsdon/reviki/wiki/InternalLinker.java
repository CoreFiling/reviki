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
package net.hillsdon.reviki.wiki;

import static java.lang.String.format;
import static net.hillsdon.reviki.text.WikiWordUtils.isAcronym;
import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;

public class InternalLinker {

  private final PageStore _store;
  private final String _wikiName;
  private final String _contextPath;

  public InternalLinker(final String contextPath, final String wikiName, final CachingPageStore store) {
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
    // Special case: only link acronyms with real pages.
    final boolean exists = exists(pageName);
    if (!exists && isAcronym(pageName)) {
      return Escape.html(pageName);
    }
    
    final String otherAttrs = exists ? "" : "rel='nofollow' ";
    final String cssClass = exists ? "existing-page" : "new-page";
    return format("<a %sclass='%s' href='%s'>%s</a>",
      otherAttrs,
      cssClass,
      Escape.html(url(pageName)),
      Escape.html(linkText)
    );
  }
  
}
