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
package net.hillsdon.reviki.web.common;

import javax.servlet.http.HttpServletRequest;


/**
 * Utility methods for getting data out of requests.
 * 
 * @author mth
 */
public final class RequestParameterReaders {


  public static String getRequiredString(final HttpServletRequest request, final String parameter) throws InvalidInputException {
    String value = request.getParameter(parameter);
    if (value == null) {
      throw new InvalidInputException(String.format("'%s' required.", parameter));
    }
    return value.trim();
  }

  public static String getString(final HttpServletRequest request, final String parameter) throws InvalidInputException {
    String value = request.getParameter(parameter);
    return value == null ? null : value.trim();
  }
  
  public static Long getLong(final String value, final String parameter) throws InvalidInputException {
    if (value == null) {
      return null;
    }
    try {
      return Long.parseLong(value);
    }
    catch (NumberFormatException ex) {
      throw new InvalidInputException(String.format("'%s' invalid.", parameter));
    }
  }

  public static final String PARAM_REVISION = "revision";
  
  private RequestParameterReaders() {
  }

  public static long getRevision(final HttpServletRequest request) throws InvalidInputException {
    Long givenRevision = getLong(request.getParameter(PARAM_REVISION), PARAM_REVISION);
    return givenRevision == null ? -1 : givenRevision;
  }

}
