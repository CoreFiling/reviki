package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result.RenderedImage;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Image extends ImmutableRenderNode {
  protected String wiki;

  protected String target;

  private LinkPartsHandler handler;

  private String title;

  public Image(String target, String title, final LinkPartsHandler handler) {
    String[] parts = target.split(":");

    // TODO: This turns links like Foo:Bar:Baz into Foo:Bar.
    if (parts.length == 1 || target.contains("://")) {
      this.wiki = null;
      this.target = target;
    }
    else {
      this.wiki = parts[0];
      this.target = parts[1];
    }

    this.title = title;
    this.handler = handler;
  }
  
  public Image(String target, final LinkPartsHandler handler) {
    this(target, target, handler);
  }

  public List<RenderNode> getChildren() {
    List<RenderNode> out = new ArrayList<RenderNode>();
    return out;
  }

  public List<ResultNode> render(PageInfo page, String text, RenderNode parent, URLOutputFilter urlOutputFilter) {
    // TODO: Handle last two params properly
    LinkParts parts = new LinkParts(target, wiki, title, null, null);
    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(new RenderedImage(page, this, parts, urlOutputFilter, handler));
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
