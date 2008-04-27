package net.hillsdon.reviki.configuration;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.core.Transform;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAware;
import net.hillsdon.reviki.web.vcintegration.RequestLocal;
import net.hillsdon.reviki.wiki.WikiUrls;

public class RequestScopedApplicationUrls implements ApplicationUrls, RequestLifecycleAware {

  private RequestLocal<ApplicationUrls> _requestLocal;

  public RequestScopedApplicationUrls() {
    _requestLocal = new RequestLocal<ApplicationUrls>(new Transform<HttpServletRequest, ApplicationUrls>() {
      public ApplicationUrls transform(final HttpServletRequest in) {
        return new ApplicationUrlsImpl(in);
      }
    });
  }
  
  public WikiUrls get(final String name) {
    return _requestLocal.get().get(name);
  }

  public String list() {
    return _requestLocal.get().list();
  }

  public String url(final String relative) {
    return _requestLocal.get().url(relative);
  }

  public void create(final HttpServletRequest request) {
    _requestLocal.create(request);
  }

  public void destroy() {
    _requestLocal.destroy();
  }

}
