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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.pages.Page;
import net.hillsdon.reviki.web.pages.SpecialPage;

/**
 * Delegates everything.  Implementors will need to provide a name
 * and the variant behaviour.
 *
 * @author mth
 */
public abstract class AbstractSpecialPage implements SpecialPage {

  private final Page _delegate;

  public AbstractSpecialPage(final Page delegate) {
    _delegate = delegate;
  }

  public View attach(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().attach(page, path, request, response);
  }

  public View attachment(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().attachment(page, path, request, response);
  }

  public View attachments(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().attachments(page, path, request, response);
  }

  public View deleteAttachment(final PageReference pageReference, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().deleteAttachment(pageReference, path, request, response);
  }

  public View editor(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().editor(page, path, request, response);
  }

  public View get(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().get(page, path, request, response);
  }

  public View history(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().history(page, path, request, response);
  }

  public View set(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    return getDelegate().set(page, path, request, response);
  }

  protected final Page getDelegate() {
    return _delegate;
  }

}
