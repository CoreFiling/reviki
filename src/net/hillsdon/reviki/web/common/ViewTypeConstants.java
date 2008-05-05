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
   * Plain text.
   */
  public static final String CTYPE_TEXT = "txt";
  
  /**
   * Latest published atom standard.
   */
  public static final String CTYPE_ATOM = "atom";
  
  public static boolean is(final HttpServletRequest request, final String type) {
    return type != null && type.equals(request.getParameter(PARAM_CTYPE));
  }
  
  private ViewTypeConstants() {
  }
  
}
