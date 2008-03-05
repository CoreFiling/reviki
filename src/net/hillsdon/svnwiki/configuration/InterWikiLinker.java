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
package net.hillsdon.svnwiki.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;

/**
 * Can create links to external wikis given a wiki name and page name.
 * 
 * @author mth
 */
public class InterWikiLinker {

  private Map<String, String> _links = new LinkedHashMap<String, String>();

  /**
   * @param wikiName Wiki name.  Will overwrite any previous entry with the same wiki name.
   * @param formatString Absolute URI template with one %s which will be replaced by the page name when creating links.
   */
  public void addWiki(final String wikiName, final String formatString) {
    _links.put(wikiName, formatString);
  }

  /**
   * @param wikiName Wiki name.
   * @param pageName Page name/
   * @return A link.
   * @throws UnknownWikiException If wikiName is unknown.
   * @see #addWiki(String, String)
   */
  public String url(final String wikiName, final String pageName) throws UnknownWikiException {
    String formatString = _links.get(wikiName);
    if (formatString == null) {
      throw new UnknownWikiException();
    }
    return String.format(formatString, Escape.url(pageName));
  }
  
  /**
   * Exposed for testing.
   * @return Unmodifiable map from wiki name for format string as provided in {@link #addWiki(String, String)}. 
   */
  Map<String, String> getWikiToFormatStringMap() {
    return Collections.unmodifiableMap(_links);
  }
  
}
