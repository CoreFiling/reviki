package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleASTBuilder;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class MacroNode extends BlockableNode<MacroNode> {

  private static final Log LOG = LogFactory.getLog(MacroNode.class);

  private String name;

  private String args;

  private final PageInfo page;

  private final CreoleASTBuilder visitor;

  public boolean block = false;

  public MacroNode(String name, String args, final PageInfo page, final CreoleASTBuilder visitor) {
    super("", null, null);
    this.name = name;
    this.args = args;
    this.page = page;
    this.visitor = visitor;

  }

  public String toXHTML() {
    String tag = block ? "pre" : "code";
    return String.format("<%s>%s</%s>", tag, Escape.html("<<" + name + ":" + args + ">>"), tag);
  }

  @Override
  public ASTNode expandMacros(Supplier<List<Macro>> macros) {
    // This is basically lifted from the old MacroNode.
    List<Macro> theMacros = macros.get();
    try {
      for (Macro macro : theMacros) {
        if (macro.getName().equals(name)) {
          String content = macro.handle(page, args);
          switch (macro.getResultFormat()) {
            case XHTML:
              return new Raw(content);
            case WIKI:
              return CreoleRenderer.renderPartWithVisitor(content, visitor, macros);
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

  @Override
  public MacroNode toBlock() {
    MacroNode block = new MacroNode(name, args, page, visitor);
    block.block = true;
    return block;
  }
}
