package net.hillsdon.reviki.wiki.renderer.creole.ast;

import com.google.common.base.Optional;

import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTRenderer.Languages;

public class InlineCode extends TextNode implements BlockableNode<Code> {
  private final Optional<Languages> _language;

  public InlineCode(final String contents, final Languages language) {
    super(contents, true);

    _language = Optional.of(language);
  }

  public InlineCode(final String contents) {
    super(contents, true);

    _language = Optional.<Languages>absent();
  }

  public Code toBlock() {
    if (_language == null) {
      return new Code(getText());
    }
    else {
      return _language.isPresent() ? new Code(getText(), _language.get()) : new Code(getText());
    }
  }

  public Optional<Languages> getLanguage() {
    return _language;
  }
}
