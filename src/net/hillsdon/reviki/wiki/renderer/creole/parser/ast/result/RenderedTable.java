package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class RenderedTable implements ResultNode {

  protected List<ResultNode> rows;

  public RenderedTable(List<ResultNode> rows) {
    this.rows = rows;
  }

  public List<ResultNode> getChildren() {
    return Collections.unmodifiableList(rows);
  }

  public String toXHTML() {
    String out = "<table>";

    for (ResultNode node : rows) {
      out += node.toXHTML();
    }

    out += "</table>";

    return out;
  }
}
