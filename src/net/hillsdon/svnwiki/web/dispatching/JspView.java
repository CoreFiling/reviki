package net.hillsdon.svnwiki.web.dispatching;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JspView implements View {

  private final String _name;

  public JspView(final String name) {
    _name = name;
  }
  
  public void render(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    request.getRequestDispatcher("/WEB-INF/templates/" + _name + ".jsp").include(request, response);
  }

}
