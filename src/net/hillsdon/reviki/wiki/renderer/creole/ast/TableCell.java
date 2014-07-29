package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;
import java.util.Map;

public class TableCell extends TaggedNode {
  public TableCell(final ASTNode body) {
    super("td", body);
  }

  @Override
  public String toXHTML(Map<String, List<String>> enabledDirectives) {
    if (!enabledDirectives.containsKey(Table.TABLE_ALIGNMENT_DIRECTIVE)) {
      return super.toXHTML(enabledDirectives);
    }

    try {
      String out = "<td " + CSS_CLASS_ATTR;
      out += " style='vertical-align:" + enabledDirectives.get(Table.TABLE_ALIGNMENT_DIRECTIVE).get(0) + "'>";
      out += innerXHTML(enabledDirectives);
      out += "</td>";
      return out;
    }
    catch (Exception e) {
      System.err.println("Error when handling directive " + Table.TABLE_ALIGNMENT_DIRECTIVE);
      return super.toXHTML(enabledDirectives);
    }
  }
}
