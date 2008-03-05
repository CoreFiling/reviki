/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.svnwiki.wiki.renderer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.creole.AbstractRegexNode;

/**
 * Macro
 * 
 * @author mth
 */
public class MacroNode extends AbstractRegexNode {

  private final Map<String, Macro> _macros;

  public MacroNode(final Collection<Macro> macros, final boolean block) {
    super(block ? "(?s)(?:^|\\n)<<([\\p{Digit}\\p{L}]+?):(.+?)(^|\\n)>>"
                : "(?s)\\<<([\\p{Digit}\\p{L}]+?):(.+?)>>");
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

  public String handle(final PageReference page, final Matcher matcher) {
    Macro macro = _macros.get(getMacroName(matcher));
    return macro.handle(page, matcher.group(2));
  }

}
