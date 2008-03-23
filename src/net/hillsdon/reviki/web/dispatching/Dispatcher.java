package net.hillsdon.reviki.web.dispatching;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Dispatcher {
  
  void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;

}
