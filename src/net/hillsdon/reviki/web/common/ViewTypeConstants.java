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
package net.hillsdon.reviki.web.common;

import javax.servlet.http.HttpServletRequest;

/**
 * Constants for view selecting parameters.
 * 
 * Fuller object that includes mime-type would be better.
 * 
 * @author mth
 */
public final class ViewTypeConstants {

  /**
   * Given to select a non-default view type.
   */
  public static final String PARAM_CTYPE = "ctype";

  /**
   * The default view.
   */
  public static final String CTYPE_DEFAULT = "default";

  /**
   * Plain text.
   */
  public static final String CTYPE_TEXT = "txt";

  /**
   * Latest published atom standard.
   */
  public static final String CTYPE_ATOM = "atom";

  /**
   * Hmm.  A little dubious.
   */
  public static final String CTYPE_RAW = "raw";

  /**
   * Docbook XML.
   */
  public static final String CTYPE_DOCBOOK = "docbook";

  public static final String CTYPE_XSLFO = "xslfo";

  public static final String CTYPE_RTF = "rtf";

  public static final String CTYPE_PDF = "pdf";

  public static final String CTYPE_PS = "postscript";

  public static final String CTYPE_DOCX = "docx";

  /**
   * List of ctypes available to regular page views.
   *
   * This is used to generate the drop-down list.
   */
  public static final String[] CTYPES = { CTYPE_DEFAULT, CTYPE_PDF, CTYPE_DOCX, CTYPE_DOCBOOK, CTYPE_RTF, CTYPE_PS, CTYPE_RAW };

  public static boolean is(final HttpServletRequest request, final String type) {
    return type != null && type.equals(request.getParameter(PARAM_CTYPE));
  }

  private ViewTypeConstants() {
  }

}
