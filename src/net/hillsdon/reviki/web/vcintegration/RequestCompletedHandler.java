package net.hillsdon.reviki.web.vcintegration;

/**
 * This lets us defer cleaning up of request locals to the top-level
 * even if they were created deeper in the call tree.  Helps give the
 * view access. 
 * 
 * @author mth
 */
public interface RequestCompletedHandler {
  
  void register(RequestLifecycleAwareManager manager);

  void requestComplete();

}
