package net.hillsdon.reviki.vc;

import java.util.Map;

import com.google.common.base.Function;

public interface PageInfo extends PageReference {

  String getWiki();

  String getContent();

  Map<String, String> getAttributes();

  PageInfo withAlternativeContent(String content);

  PageInfo withAlternativeAttributes(Map<String, String> attributes);

  SyntaxFormats getSyntax(Function<String, String> defaultSyntax);

}