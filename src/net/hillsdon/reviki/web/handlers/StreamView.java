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
package net.hillsdon.reviki.web.handlers;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import net.hillsdon.reviki.web.common.View;

/**
 * Render a stream with a given content type.
 *
 * @author msw
 */
public class StreamView implements View {
  private final String _mimeType;

  private final InputStream _contents;

  /**
   * @param mimeType The content type of the page (eg, "application/xml").
   * @param contents Source of the response body.
   */
  public StreamView(final String mimeType, final InputStream contents) {
    _mimeType = mimeType;
    _contents = contents;
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    response.setContentType(_mimeType);
    IOUtils.copy(_contents, response.getOutputStream());
  }
}
