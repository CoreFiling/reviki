package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTRenderer.Languages;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class InlineCode extends TextNode implements BlockableNode<Code> {
  private final String _contents;

  private final Optional<Languages> _language;

  public InlineCode(final String contents, final Languages language) {
    super(contents, true);

    _contents = contents;
    _language = Optional.of(language);
  }

  public InlineCode(final String contents) {
    super(contents, true);

    _contents = contents;
    _language = Optional.<Languages>absent();
  }

  public Code toBlock() {
    if (_language == null) {
      return new Code(_contents);
    }
    else {
      return _language.isPresent() ? new Code(_contents, _language.get()) : new Code(_contents);
    }
  }

  @Override
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    return this;
  }

  @Override
  public String getText() {
    return _contents;
  }

  public Optional<Languages> getLanguage() {
    return _language;
  }
}
