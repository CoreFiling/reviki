package net.hillsdon.reviki.vc;

import java.util.Map;

import net.hillsdon.reviki.vc.impl.AutoPropertiesApplier;

public interface PageInfo extends PageReference {

  String getWiki();

  String getContent();

  Map<String, String> getAttributes();

  PageInfo withAlternativeContent(String content);

  PageInfo withAlternativeAttributes(Map<String, String> attributes);

  SyntaxFormats getSyntax(AutoPropertiesApplier propsApplier);

}