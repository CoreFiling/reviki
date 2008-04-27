package net.hillsdon.reviki.web.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectToRequestURLView implements View {

  public static final View INSTANCE = new RedirectToRequestURLView();
  
  public void render(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    response.sendRedirect(request.getRequestURL().toString());
  }

}
