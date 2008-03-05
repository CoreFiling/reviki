package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageReference;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.Encoder;

public abstract class AttachedMacro extends BaseMacro {

  @Override
  public final void execute(final Writer writer, final MacroParameter params) throws IllegalArgumentException, IOException {
    PageReference page = (PageReference) params.getContext().get("page");
    String file = params.get(0);
    if (file == null) {
      throw new IllegalArgumentException("Attachment not specified.");
    }
    String url;
    int separator = file.indexOf('/');
    if (separator == -1) {
      url = page.getPath() + "/attachments/" + params.get(0);
    }
    else {
      // E.g. "AnotherPage/attachment.name"
      url = file.substring(0, separator) + "/attachments/" + file.substring(separator + 1);
    }
    write(writer, Encoder.escape(url), Encoder.escape(file));
  }

  /**
   * Write out a link.
   * @param writer Write here.
   * @param url The URL (escaped).
   * @param name The link text (escaped).
   * @throws IOException On IO error. 
   */
  protected abstract void write(Writer writer, String url, String name) throws IOException;

  @Override
  public String getName() {
    return "attached";
  }

}
