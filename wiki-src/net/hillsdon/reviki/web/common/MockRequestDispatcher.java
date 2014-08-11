package net.hillsdon.reviki.web.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockRequestDispatcher implements RequestDispatcher {
  private List<ServletRequest> _forwardedRequests = new ArrayList<ServletRequest>();
  private List<ServletRequest> _includedRequests;

  public void forward(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
    _forwardedRequests.add(arg0);
  }
  
  public List<ServletRequest> getForwardedRequests() {
    return _forwardedRequests;
  }

  public void include(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
    _includedRequests.add(arg0);
  }
  
  public List<ServletRequest> getIncludedRequests() {
    return _includedRequests;
  }
}
