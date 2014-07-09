package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class RenderedUnorderedList implements ResultNode {

  protected ResultNode body;

  protected List<ResultNode> children;

  public RenderedUnorderedList(ResultNode body, List<ResultNode> children) {
    this.body = body;
    this.children = children;
  }

  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(body);
    out.addAll(children);
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    String out = body.toXHTML();

    if (children.size() > 0) {
      out += "<ul>";

      for (ResultNode node : children) {
        out = out + "<li>" + node.toXHTML() + "</li>";
      }

      out += "</ul>";
    }

    return out;
  }
}
