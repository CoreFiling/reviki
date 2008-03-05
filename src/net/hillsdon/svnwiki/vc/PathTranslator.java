package net.hillsdon.svnwiki.vc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface PathTranslator {
  
  Pattern ATTACHMENT_PATH = Pattern.compile(".*/(.*?)-attachments/.*");
  
  PathTranslator RELATIVE = new PathTranslator() {
    public String translate(String rootPath, String path) {
      return path.substring(rootPath.length() + 1);
    }
  };
  PathTranslator ATTACHMENT_TO_PAGE = new PathTranslator() {
    public String translate(String rootPath, String path) {
      String name = RELATIVE.translate(rootPath, path);
      Matcher matcher = ATTACHMENT_PATH.matcher(path);
      if (matcher.matches()) {
        name = matcher.group(1);
      }
      return name;
    }
  };
  
  String translate(String rootPath, String path);
  
}