package net.hillsdon.reviki.web.vcintegration;

import javax.servlet.http.HttpServletRequest;

public interface RequestLifecycleAwareManager {

  public abstract void requestStarted(final HttpServletRequest request);

  public abstract void requestComplete();

}