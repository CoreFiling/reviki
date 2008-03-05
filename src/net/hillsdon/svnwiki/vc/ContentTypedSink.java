package net.hillsdon.svnwiki.vc;

import java.io.IOException;
import java.io.OutputStream;

public interface ContentTypedSink {

  void setFileName(String attachment);

  void setContentType(String contentType);
  
  OutputStream stream() throws IOException;

  
}
