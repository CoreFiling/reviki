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

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public abstract class AbstractListOfPagesMacro implements Macro {

  public final String handle(final PageReference page, final String remainder) throws Exception {
    List<String> pages = new ArrayList<String>(getPages(remainder));
    sort(pages);

    return Joiner.on("\n").join(Iterables.transform(pages, new Function<String, String>() {
      public String apply(final String page) {
        return "  * [[" + page + "]]";
      }
    }));
  }

  public final ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  protected abstract Collection<String> getPages(String remainder) throws Exception;

}
