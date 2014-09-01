package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTRenderer.Languages;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

public class Code extends TextNode {
  private final String _contents;

  private final Optional<Languages> _language;

  public Code(final String contents, final Languages language) {
    super(contents, true);

    _contents = contents;
    _language = Optional.of(language);
  }

  public Code(final String contents) {
    super(contents, true);

    _contents = contents;
    _language = Optional.<Languages>absent();
  }

  @Override
  public List<ASTNode> expandMacrosInt(final Supplier<List<Macro>> macros) {
    return ImmutableList.of((ASTNode) this);
  }

  @Override
  public String getText() {
    return _contents;
  }

  public Optional<Languages> getLanguage() {
    return _language;
  }
}
