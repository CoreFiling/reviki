package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

public class AttachedFileMacro extends AttachedMacro {

  @Override
  public String getName() {
    return "attached";
  }

  @Override
  protected void write(final Writer writer, final String url, final String name) throws IOException {
    writer.write(String.format("<a href='%s'>%s</a>", url, name));
  }

}
