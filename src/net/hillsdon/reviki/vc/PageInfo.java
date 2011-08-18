package net.hillsdon.reviki.vc;

import java.util.Map;

public interface PageInfo extends PageReference {

  String getWiki();

  String getContent();

  Map<String, String> getAttributes();

  PageInfo withAlternativeContent(String content);

  PageInfo withAlternativeAttributes(Map<String, String> attributes);

}