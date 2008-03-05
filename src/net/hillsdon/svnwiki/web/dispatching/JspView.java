package net.hillsdon.svnwiki.web.dispatching;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JspView implements View {

  private final String _name;
  private final Map<String, Object> _data;

  public JspView(final String name, final Map<String, Object> data) {
    _name = name;
    _data = data;
  }
  
  public JspView(final String name) {
    this(name, Collections.<String, Object>emptyMap());
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    for (Map.Entry<String, Object> entry : _data.entrySet()) {
      request.setAttribute(entry.getKey(), entry.getValue());
    }
    request.getRequestDispatcher("/WEB-INF/templates/" + _name + ".jsp").include(request, response);
  }

}
