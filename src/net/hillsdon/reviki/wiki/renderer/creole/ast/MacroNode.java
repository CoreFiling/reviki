package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleASTBuilder;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class MacroNode extends ASTNode implements BlockableNode<MacroNode> {

  private static final Log LOG = LogFactory.getLog(MacroNode.class);

  private final String _name;

  private final String _args;

  private final CreoleASTBuilder _visitor;

  private final boolean _block;

  public MacroNode(final String name, final String args, final CreoleASTBuilder visitor, final boolean isBlock) {
    super(new Raw(Escape.html("<<" + name + ":" + args + ">>")));
    _name = name;
    _args = args;
    _visitor = visitor;
    _block = isBlock;
  }

  /** Create a new inline macro node. */
  public MacroNode(final String name, final String args, final CreoleASTBuilder visitor) {
    this(name, args, visitor, false);
  }

  @Override
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    // This is basically lifted from the old MacroNode.
    List<Macro> theMacros = macros.get();
    try {
      for (Macro macro : theMacros) {
        if (macro.getName().equals(_name)) {
          String content = macro.handle(_visitor.page(), _args);
          switch (macro.getResultFormat()) {
            case XHTML:
              return new Raw(content);
            case WIKI:
              return CreoleRenderer.renderPartWithVisitor(content, _visitor, macros);
            default:
              return new Plaintext(content);
          }
        }
      }

    }
    catch (Exception e) {
      LOG.error("Error handling macro on: " + _visitor.page().getPath(), e);
    }

    // Failed to find a macro of the same name.
    return this;
  }

  /**
   * Whether this occurs in a block context or not.
   */
  public boolean isBlock() {
    return _block;
  }

  public MacroNode toBlock() {
    return new MacroNode(_name, _args, _visitor, true);
  }
}
