package net.hillsdon.reviki.web.vcintegration;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.core.Transform;

public abstract class AbstractRequestLifecycleAware<T> implements RequestLifecycleAware {

  private final RequestLocal<T> _requestLocal;
  
  public AbstractRequestLifecycleAware(final Transform<HttpServletRequest, T> transform) {
    _requestLocal = new RequestLocal<T>(transform);
  }
  
  public final void create(HttpServletRequest request) {
    _requestLocal.create(request);
  }

  public final void destroy() {
    _requestLocal.destroy();
  }
  
  protected T get() {
    return _requestLocal.get();
  }

}
