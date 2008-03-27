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
package net.hillsdon.reviki.web.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.View;


/**
 * Provides implementations of the key functionality for a page in the wiki.
 * 
 * @author mth
 */
public interface Page {

  View history(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;

  View get(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;
  View editor(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;
  View set(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;

  View attach(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;
  View attachment(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;
  View attachments(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
