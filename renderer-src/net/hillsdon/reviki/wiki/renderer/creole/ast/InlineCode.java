package net.hillsdon.reviki.wiki.renderer.creole.ast;

import com.google.common.base.Optional;

public class InlineCode extends TextNode {
  private final Optional<String> _language;

  public InlineCode(final String contents, final String language) {
    super(contents, true);

    _language = Optional.of(language);
  }

  public InlineCode(final String contents) {
    super(contents, true);

    _language = Optional.<String>absent();
  }

  public Optional<String> getLanguage() {
    return _language;
  }
}
