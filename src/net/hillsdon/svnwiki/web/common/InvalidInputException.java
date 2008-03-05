package net.hillsdon.svnwiki.web.common;

/**
 * Not intended for forms validation, just to guarantee required request parameters.
 * 
 * In general the exception would be handled by the top level error handler
 * and the message given displayed to the user. 
 * 
 * @author mth
 */
public class InvalidInputException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidInputException(final String message) {
    super(message);
  }
  
}
