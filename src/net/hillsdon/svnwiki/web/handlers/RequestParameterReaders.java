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
    return value;
  }

  public static long getRequiredLong(final HttpServletRequest request, final String parameter) throws InvalidInputException {
    String baseRevisionString = getRequiredString(request, parameter);
    try {
      return Long.parseLong(baseRevisionString);
    }
    catch (NumberFormatException ex) {
      throw new InvalidInputException(String.format("'%s' invalid.", parameter));
    }
  }
  
  private RequestParameterReaders() {
  }

}
