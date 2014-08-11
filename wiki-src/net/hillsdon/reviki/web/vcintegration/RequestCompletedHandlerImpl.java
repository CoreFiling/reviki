package net.hillsdon.reviki.web.vcintegration;

import java.util.ArrayList;
import java.util.List;

public class RequestCompletedHandlerImpl implements RequestCompletedHandler {

  private final List<RequestLifecycleAwareManager> _list = new ArrayList<RequestLifecycleAwareManager>();

  public void register(final RequestLifecycleAwareManager manager) {
    _list.add(manager);
  }

  public void requestComplete() {
    for (RequestLifecycleAwareManager manager : _list) {
      manager.requestComplete();
    }
  }

}
