package net.hillsdon.svnwiki.wiki.renderer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;

public class MacroNode extends AbstractRegexNode {

  private final Map<String, Macro> _macros;

  public MacroNode(final Collection<Macro> macros) {
    super("[{](.+?):(.+?)[}]");
    _macros = new LinkedHashMap<String, Macro>();
    for (Macro macro : macros) {
      _macros.put(macro.getName(), macro);
    }
  }

  @Override
  protected boolean confirmMatchFind(final Matcher matcher) {
    return _macros.containsKey(getMacroName(matcher));
  }

  private String getMacroName(final Matcher matcher) {
    return matcher.group(1).trim();
  }

  public String handle(PageReference page, final Matcher matcher) {
    Macro macro = _macros.get(getMacroName(matcher));
    return macro.handle(page, matcher.group(2));
  }

}
