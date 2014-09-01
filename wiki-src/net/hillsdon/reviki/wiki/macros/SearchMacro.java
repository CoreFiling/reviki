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
package net.hillsdon.reviki.wiki.macros;

import java.util.Collection;

import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class SearchMacro extends AbstractListOfPagesMacro {

  private final SearchEngine _searchEngine;

  public SearchMacro(final SearchEngine searchEngine) {
    _searchEngine = searchEngine;
  }

  public String getName() {
    return "search";
  }

  @Override
  protected Collection<String> getPages(final String remainder) throws Exception {
    String query = escapeQueryForSearch(remainder);
    return ImmutableList.copyOf(Iterables.transform(_searchEngine.search(query, false, true), SearchMatch.TO_PAGE_NAME));
  }

  // convert attributes from @(")?attrKey(")?:(")?attrValue(")? into
  // properly escaped attributes:"attrKey":"attrValue" form accepted by searcher
  private String escapeQueryForSearch(String query) {
    StringBuilder searchQuery = new StringBuilder();
    String initialQuery = query.trim();
    int attributeStart = initialQuery.indexOf('@');
    int keyStart, keyEnd, colonIndex, valueStart, valueEnd, attributeEnd;
    String key, value;
    while(attributeStart >= 0) {
      searchQuery.append(initialQuery.substring(0, attributeStart) + " ");
      initialQuery = initialQuery.substring(attributeStart + 1).trim();
      if (initialQuery.startsWith("\"")) {
        keyStart = 1;
        keyEnd = initialQuery.indexOf('"', keyStart);
        colonIndex = initialQuery.indexOf(':', keyEnd);
      }
      else {
        keyStart = 0;
        keyEnd = initialQuery.indexOf(':');
        colonIndex = keyEnd;
      }
      key = initialQuery.substring(keyStart, keyEnd).trim();
      initialQuery = initialQuery.substring(colonIndex + 1).trim();
      if (initialQuery.startsWith("\"")) {
        valueStart = 1;
        valueEnd = initialQuery.indexOf('"', valueStart);
        attributeEnd = valueEnd + 1;
      }
      else {
        valueStart = 0;
        valueEnd = initialQuery.indexOf(' ');
        if (valueEnd == -1) {
          valueEnd = initialQuery.indexOf(')');
          if (valueEnd == -1) {
            valueEnd = initialQuery.length();
          }
        }
        attributeEnd = valueEnd;
      }
      value = initialQuery.substring(valueStart, valueEnd).trim();
      if(attributeEnd < initialQuery.length()) {
        initialQuery = initialQuery.substring(attributeEnd).trim();
      }
      else {
        initialQuery = "";
      }
      attributeStart = initialQuery.indexOf('@');
      searchQuery.append("attributes:\"" + _searchEngine.escape("\"" + key + "\":\"" + value + "\"") + "\" ");
    }
    searchQuery.append(initialQuery);
    return searchQuery.toString();
  }
}