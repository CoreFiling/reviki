package net.hillsdon.reviki.wiki.macros;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

public class AttrMacro implements Macro {

  public static final String REVIKI_ATTRIBUTE_PREFIX = "reviki:";
  private PageStore _store;

  public AttrMacro(PageStore store) {
    _store = store;
  }

  public String getName() {
    return "attr";
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  public String handle(final PageInfo page, final String remainder) throws Exception {
    String regexp = "((\\p{Alnum}+)(\\.))?(\\\")?([^\\\"]+)(\\\")?";
    Pattern pattern = Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(remainder);
    if(matcher.find()) {
      String pagePart = matcher.group(2);
      String attrPart = matcher.group(5);
      final PageReference pageRef;
      if(pagePart == null) {
        Map<String, String> attributes = page.getAttributes();
        if(attributes != null && !attributes.isEmpty()) {
          String attrValue = attributes.get(attrPart);
          if(attrValue == null) {
            return "";
          }
          return attrValue;
        }
        pageRef = page;
      }
      else {
        pageRef = new PageReferenceImpl(pagePart);
      }
      final VersionedPageInfo pageInfo = _store.get(pageRef, -1);
      Map<String, String> pageAttributes = pageInfo.getAttributes();
      String attrValue = pageAttributes.get(attrPart);
      if(attrValue == null) {
        return "";
      }
      return attrValue;
    }
    return "";
  }

}
