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
package net.hillsdon.reviki.wiki.macros;

import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.Functional.map;

import java.util.Collection;

import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;

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
    return list(map(_searchEngine.search(remainder, false), SearchMatch.TO_PAGE_NAME));
  }

}
