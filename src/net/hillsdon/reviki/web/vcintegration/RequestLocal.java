package net.hillsdon.reviki.web.vcintegration;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.accessors.Accessor;
import net.hillsdon.fij.core.Transform;

/**
 * A thread-local bound to the request lifecycle.
 * 
 * @author mth
 *
 * @param <T> Class.
 */
public class RequestLocal<T> implements Accessor<T>, RequestLifecycleAware {

  private final ThreadLocal<T> _threadLocal = new ThreadLocal<T>();
  private final Transform<HttpServletRequest, T> _factory;

  public RequestLocal(final Transform<HttpServletRequest, T> factory) {
    _factory = factory;
  }

  public void create(final HttpServletRequest request) {
    _threadLocal.set(_factory.transform(request));
  }
  
  public void destroy() {
    _threadLocal.set(null);
  }

  public T get() {
    return _threadLocal.get();
  }

}
