package net.hillsdon.reviki.vc;

import java.io.IOException;

public interface ChangeNotificationDospatcher {

  void sync() throws PageStoreAuthenticationException, PageStoreException, IOException;

}
