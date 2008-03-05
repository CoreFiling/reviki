package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

public class AttachedImageMacro extends AttachedMacro {

  @Override
  public String getName() {
    return "image";
  }

  @Override
  protected void write(final Writer writer, final String url, final String name) throws IOException {
    writer.write(String.format("<img src='%s'/>", url));
  }

}