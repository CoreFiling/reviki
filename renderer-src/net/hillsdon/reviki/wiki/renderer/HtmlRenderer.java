package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;

public abstract class HtmlRenderer extends MarkupRenderer<String> {

  @Override
  public abstract String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException;

  @Override
  public abstract ASTNode parse(final PageInfo page) throws IOException, PageStoreException;

  public abstract LinkPartsHandler getLinkPartsHandler();

}