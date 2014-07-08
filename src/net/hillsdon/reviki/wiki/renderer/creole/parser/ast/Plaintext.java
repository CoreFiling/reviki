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
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result.RenderedPlaintext;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Plaintext extends ImmutableRenderNode {

  protected String contents;
  
  public Plaintext(String contents) {
    this.contents = contents;
  }

  public List<RenderNode> getChildren() {
    List<RenderNode> out = new ArrayList<RenderNode>();
    return Collections.unmodifiableList(out);
  }

  public List<ResultNode> render(PageInfo page, String text, RenderNode parent, URLOutputFilter urlOutputFilter) {
    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(new RenderedPlaintext(contents));
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
