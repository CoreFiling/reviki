package net.hillsdon.reviki.wiki.renderer.creole.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class CreoleRenderer {
  public static ResultNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    ANTLRInputStream in = new ANTLRInputStream(page.getContent());
    CreoleTokens lexer = new CreoleTokens(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    ParseTree tree = parser.creole();

    ParseTreeVisitor<ResultNode> visitor = new Visitor(page, urlOutputFilter, handler);

    return visitor.visit(tree);
  }
}
