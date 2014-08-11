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
package net.hillsdon.reviki.web.pages.impl;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.pages.Page;
import net.hillsdon.reviki.web.pages.PageSource;
import net.hillsdon.reviki.web.pages.SpecialPages;

public class PageSourceImpl implements PageSource {

  private final SpecialPages _specialPages;
  private final DefaultPage _defaultPage;

  public PageSourceImpl(final SpecialPages specialPages, final DefaultPage defaultPage) {
    _specialPages = specialPages;
    _defaultPage = defaultPage;
  }

  public Page get(final PageReference pageReference) {
    Page page = _specialPages.get(pageReference.getPath());
    return page != null ? page : _defaultPage;
  }

}
