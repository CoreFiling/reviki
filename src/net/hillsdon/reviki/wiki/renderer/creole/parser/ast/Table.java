package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result.RenderedTable;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Table extends ImmutableRenderNode {
  protected List<RenderNode> rows;

  public Table(RenderNode body, List<RenderNode> rows) {
    this.rows = rows;
  }

  public Table(RenderNode body) {
    this(body, new ArrayList<RenderNode>());
  }

  public Table(List<RenderNode> children) {
    this(new Plaintext(""), children);
  }

  public List<RenderNode> getChildren() {
    return Collections.unmodifiableList(rows);
  }

  public List<ResultNode> render(PageInfo page, String text, RenderNode parent, URLOutputFilter urlOutputFilter) {
    List<ResultNode> rows = new ArrayList<ResultNode>();

    for (RenderNode node : this.rows) {
      List<ResultNode> res = node.render(page, text, this, urlOutputFilter);
      assert (res.size() == 1);
      rows.add(res.get(0));
    }

    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(new RenderedTable(rows));
    return out;
  }

  public Matcher find(String text) {
    // TODO Auto-generated method stub
    return null;
  }

  public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    // TODO Auto-generated method stub
    return null;
  }
}
