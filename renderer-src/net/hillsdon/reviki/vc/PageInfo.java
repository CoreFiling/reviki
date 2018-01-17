package net.hillsdon.reviki.vc;

import java.util.Map;

import com.google.common.base.Function;

public interface PageInfo extends PageReference {

  String getWiki();

  String getContent();

  Map<String, String> getAttributes();

  PageInfo withAlternativeContent(String content);

  PageInfo withAlternativeAttributes(Map<String, String> attributes);

  /**
   * @param defaultSyntaxFromFileName Function. Should return the name of a
   *          {@link SyntaxFormats} member but returning something else will be
   *          gracefully assumed to mean {@link SyntaxFormats#REVIKI}
   * @return Syntax format for this page.
   */
  SyntaxFormats getSyntax(Function<String, String> defaultSyntaxFromFileName);

}