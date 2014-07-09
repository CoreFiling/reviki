package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class TableHeaderCell implements ResultNode {
  protected ResultNode body;

  public TableHeaderCell(ResultNode body) {
    this.body = body;
  }

  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(body);
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    String content = body.toXHTML();

    if (content.equals("")) {
      return "<th/>";
    }

    return "<th>" + content + "</th>";
  }
}
