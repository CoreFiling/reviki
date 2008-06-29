package net.hillsdon.reviki.web.vcintegration;

import java.util.ArrayList;
import java.util.List;

public class RequestCompletedHandlerImpl implements RequestCompletedHandler {

  private final ThreadLocal<List<RequestLifecycleAwareManager>> _list = new ThreadLocal<List<RequestLifecycleAwareManager>>();
  
  public void register(final RequestLifecycleAwareManager manager) {
    ensureList().add(manager);
  }

  private List<RequestLifecycleAwareManager> ensureList() {
    List<RequestLifecycleAwareManager> list = _list.get();
    if (list == null) {
      list = new ArrayList<RequestLifecycleAwareManager>();
      _list.set(list);
    }
    return list;
  }

  public void requestComplete() {
    for (RequestLifecycleAwareManager manager : ensureList()) {
      manager.requestComplete();
    }
    _list.set(null);
  }

}
