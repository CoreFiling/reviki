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

import java.util.List;
import java.util.regex.Matcher;

import net.hillsdon.fij.accessors.Accessor;
import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.creole.AbstractRegexNode;
import net.hillsdon.svnwiki.wiki.renderer.creole.RenderNode;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Macro
 * 
 * @author mth
 */
public class MacroNode extends AbstractRegexNode {

  private static final Log LOG = LogFactory.getLog(MacroNode.class);
  
  private final Accessor<List<Macro>> _macros;

  public MacroNode(final Accessor<List<Macro>> macros, final boolean block) {
    super(block ? "(?s)(?:^|\\n)<<([-\\p{Digit}\\p{L}]+?):(.+?)(^|\\n)>>"
                : "(?s)\\<<([-\\p{Digit}\\p{L}]+?):(.+?)>>");
    _macros = macros;
  }

  private String getMacroName(final Matcher matcher) {
    return matcher.group(1).trim();
  }

  public String handle(final PageReference page, final Matcher matcher, RenderNode parent) {
    // We need to move to a push system for updating macros to avoid this.
    final String macroName = getMacroName(matcher);
    Macro macro = null;
    List<Macro> macros = _macros.get();
    for (Macro candidate : macros) {
      if (candidate.getName().equals(macroName)) {
        macro = candidate;
        break;
      }
    }
    if (macro == null) {
      return matcher.group();
    }
    
    try {
      String content = macro.handle(page, matcher.group(2));
      switch (macro.getResultFormat()) {
        case XHTML:
          return content;
        case WIKI:
          // Use the parent as renderer if possible as that has the appropriate child nodes.
          RenderNode renderer = parent != null ? parent : this;
          return renderer.render(page, content, this);
        default:
          return Escape.html(content);
      }
    }
    catch (Exception e) {
      LOG.error("Error handling macro on: " + page.getPath(), e);
      return String.format("<p>Error evaluating macro '%s': %s</p>", Escape.html(macro.getName()), Escape.html(e.getMessage()));
    }
  }

}
