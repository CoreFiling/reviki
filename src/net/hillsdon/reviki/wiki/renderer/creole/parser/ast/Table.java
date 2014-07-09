package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Table implements ResultNode {

  protected List<ResultNode> rows;

  public Table(List<ResultNode> rows) {
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
