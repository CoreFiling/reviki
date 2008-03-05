package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;


public class AttachedMacro implements Macro {

  private final String _formatString;
  private final String _name;
  
  public AttachedMacro(final String name, final String formatString) {
    _name = name;
    _formatString = formatString;
  }
  
  public String getName() {
    return _name;
  }

  public String handle(final PageReference page, final String remainder) {
    final String attachmentPath = page.getPath() + "/attachments/" + remainder;
    return String.format(_formatString, attachmentPath, Escape.html(remainder));
  }
  
}
