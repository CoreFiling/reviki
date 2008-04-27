package net.hillsdon.reviki.web.vcintegration;

import javax.servlet.http.HttpServletRequest;

public class RequestLifecycleAwareManagerImpl implements RequestLifecycleAwareManager {

  private final RequestLifecycleAware[] _requestLifecycleAware;

  public RequestLifecycleAwareManagerImpl(final RequestLifecycleAware... requestLifecycleAware) {
    _requestLifecycleAware = requestLifecycleAware;
  }
  
  public void requestStarted(final HttpServletRequest request) {
    for (RequestLifecycleAware aware : _requestLifecycleAware) {
      aware.create(request);
    }
  }

  public void requestComplete() {
    for (RequestLifecycleAware aware : _requestLifecycleAware) {
      aware.destroy();
    }
  }
  
}
