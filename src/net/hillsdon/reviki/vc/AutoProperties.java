package net.hillsdon.reviki.vc;

import java.util.Map;

public interface AutoProperties {
  
  Map<String, String> read() throws PageStoreException;
  
}
