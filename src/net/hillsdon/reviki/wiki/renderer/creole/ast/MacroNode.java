package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class MacroNode extends ASTNode {

  private static final Log LOG = LogFactory.getLog(MacroNode.class);

  private String name;

  private String args;

  private URLOutputFilter urlOutputFilter;

  private LinkPartsHandler linkHandler;

  private LinkPartsHandler imageHandler;

  private PageInfo page;

  public MacroNode(String name, String args, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    super("", null, null);
    this.name = name;
    this.args = args;
    this.page = page;
    this.urlOutputFilter = urlOutputFilter;
    this.linkHandler = linkHandler;
    this.imageHandler = imageHandler;

  }

  public String toXHTML() {
    // If the macro is converted into HTML before being expanded, return it as
    // literal text.
    return "<code>" + Escape.html("<<" + name + ":" + args + ">>") + "</code>";
  }

  @Override
  public ASTNode expandMacros(List<Macro> macros) {
    // This is basically lifted from the old MacroNode.
    try {
      for (Macro macro : macros) {
        if (macro.getName().equals(name)) {
          String content = macro.handle(page, args);
          switch (macro.getResultFormat()) {
            case XHTML:
              return new Raw(content);
            case WIKI:
              return CreoleRenderer.renderPart(page, content, urlOutputFilter, linkHandler, imageHandler, macros);
            default:
              return new Plaintext(content);
          }
        }
      }

    }
    catch (Exception e) {
      LOG.error("Error handling macro on: " + page.getPath(), e);
    }

    // Failed to find a macro of the same name.
    return this;
  }
}
