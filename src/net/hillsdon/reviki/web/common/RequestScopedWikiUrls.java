package net.hillsdon.reviki.web.common;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.core.Transform;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAware;
import net.hillsdon.reviki.web.vcintegration.RequestLocal;
import net.hillsdon.reviki.wiki.WikiUrls;

public class RequestScopedWikiUrls implements WikiUrls, RequestLifecycleAware {

  private RequestLocal<WikiUrls> _requestLocal;

  public RequestScopedWikiUrls(final WikiConfiguration configuration) {
    _requestLocal = new RequestLocal<WikiUrls>(new Transform<HttpServletRequest, WikiUrls>() {
      public WikiUrls transform(final HttpServletRequest in) {
        return new WikiUrlsImpl(in, configuration);
      }
    });
  }

  public String favicon() {
    return _requestLocal.get().favicon();
  }

  public String feed() {
    return _requestLocal.get().feed();
  }

  public String page(String name) {
    return _requestLocal.get().page(name);
  }

  public String root() {
    return _requestLocal.get().root();
  }

  public String search() {
    return _requestLocal.get().search();
  }

  public void create(final HttpServletRequest request) {
    _requestLocal.create(request);
  }

  public void destroy() {
    _requestLocal.destroy();
  }
  
}
