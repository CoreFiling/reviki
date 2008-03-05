package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageReference;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.Encoder;

public class AttachedMacro extends BaseMacro {

  @Override
  public void execute(final Writer writer, final MacroParameter params) throws IllegalArgumentException, IOException {
    PageReference page = (PageReference) params.getContext().get("page");
    String url = page.getPath() + "/attachments/" + params.get(0);
    writer.write(String.format("<a href='%s'>%s</a>", Encoder.escape(url), Encoder.escape(params.get(0))));
  }

  @Override
  public String getName() {
    return "attached";
  }

}
