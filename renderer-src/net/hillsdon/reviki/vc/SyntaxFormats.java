package net.hillsdon.reviki.vc;

import java.util.Locale;

public enum SyntaxFormats {

  REVIKI("Reviki"),
  MARKDOWN("Markdown");

  private final String _displayName;

  private SyntaxFormats(final String displayName) {
    _displayName = displayName;
  }

  public static SyntaxFormats fromValue(final String value) {
    try {
      return valueOf(value.toUpperCase(Locale.ENGLISH));
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  public String value() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  @Override
  public String toString() {
    return _displayName;
  }

}
