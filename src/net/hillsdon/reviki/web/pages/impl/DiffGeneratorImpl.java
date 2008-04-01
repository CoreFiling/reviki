package net.hillsdon.reviki.web.pages.impl;

import java.util.LinkedList;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.web.pages.DiffGenerator;

import com.google.code.diff_match_patch.Diff;
import com.google.code.diff_match_patch.diff_match_patch;
import com.google.code.diff_match_patch.diff_match_patch.Operation;

public class DiffGeneratorImpl implements DiffGenerator {

  private diff_match_patch _api;

  public DiffGeneratorImpl() {
    _api = new diff_match_patch();
  }
  
  public String getDiffMarkup(String base, String head) {
    LinkedList<Diff> diffs = _api.diff_main(base, head);
    _api.diff_cleanupSemantic(diffs);
    return prettyXHTML(diffs);
  }
  
  /**
   * Generates ins, del, span elements for additions, removals and unchanged
   * text respectively, all with class 'diff'.
   * 
   * @param diffs Diffs.
   * @return The XHTML.
   */
  public String prettyXHTML(final LinkedList<Diff> diffs) {
    String xhtml = "";
    for (Diff diff : diffs) {
      String text = diff.text;
      text = Escape.html(text);
      text = text.replaceAll("\n", "<br />");
      xhtml += tag(diff.operation, text);
    }
    return xhtml;
  }

  private String tag(final Operation operation, final String text) {
    final String tag = tagForOperation(operation);
    return "<" + tag + " class='diff'>" + text + "</" + tag + ">";
  }

  private String tagForOperation(final Operation operation) {
    switch (operation) {
      case INSERT:
        return "ins";
      case DELETE:
        return "del";
      default:
        return "span";
    }
  }

}
