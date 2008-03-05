package net.hillsdon.svnwiki.web.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {

  /**
   * Does nothing.
   */
  View NULL = new View() {
    public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }
  };

  void render(HttpServletRequest request, HttpServletResponse response) throws Exception;
  
}
