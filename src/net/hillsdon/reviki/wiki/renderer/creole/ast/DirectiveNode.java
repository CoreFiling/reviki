package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

public class DirectiveNode extends ASTNode {
  private final String _name;

  private final boolean _enabled;

  private final ImmutableList<String> _args;

  public DirectiveNode(final String name, final boolean enabled, final List<String> args) {
    _name = name;
    _enabled = enabled;
    _args = ImmutableList.copyOf(args);
  }

  public DirectiveNode(final String name, final boolean enabled, final String args) {
    ImmutableList.Builder<String> splitArgs = new ImmutableList.Builder<String>();

    for (String arg : args.split(",")) {
      splitArgs.add(arg.trim());
    }

    _name = name;
    _enabled = enabled;
    _args = splitArgs.build();
  }

  public DirectiveNode(final String name, final boolean enabled) {
    this(name, enabled, ImmutableList.<String> of());
  }

  public String getName() {
    return _name;
  }

  public boolean isEnabled() {
    return _enabled;
  }

  public List<String> getArgs() {
    return _args;
  }

  @Override
  public String toXHTML() {
    return "";
  }

  @Override
  public ASTNode expandMacros(Supplier<List<Macro>> macros) {
    return this;
  }
}
