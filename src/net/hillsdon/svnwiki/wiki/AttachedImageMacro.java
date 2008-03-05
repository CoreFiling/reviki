package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageReference;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.Encoder;

public class AttachedImageMacro extends BaseMacro {

  @Override
  public void execute(final Writer writer, final MacroParameter params) throws IllegalArgumentException, IOException {
    PageReference page = (PageReference) params.getContext().get("page");
    String url = page.getPath() + "/attachments/" + params.get(0);
    writer.write(String.format("<img src='%s'/>", Encoder.escape(url)));
  }

  @Override
  public String getName() {
    return "image";
  }

}
