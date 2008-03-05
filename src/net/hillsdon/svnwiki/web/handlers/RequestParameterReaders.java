package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.svnwiki.web.InvalidInputException;

/**
 * Utility methods for getting data out of requests.
 * 
 * @author mth
 */
final class RequestParameterReaders {


  public static String getRequiredString(final HttpServletRequest request, final String parameter) throws InvalidInputException {
    String value = request.getParameter(parameter);
    if (value == null) {
      throw new InvalidInputException(String.format("'%s' required.", parameter));
    }
    return value.trim();
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
