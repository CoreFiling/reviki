package net.hillsdon.reviki.web.vcintegration;

import javax.servlet.http.HttpServletRequest;

public interface RequestLifecycleAware {

  void create(final HttpServletRequest request);

  void destroy();

}