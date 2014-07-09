package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class CreoleRenderer {
  public static ResultNode render(final PageInfo page, final URLOutputFilter urlOutputFilter) {
    ANTLRInputStream in = new ANTLRInputStream(page.getContent());
    return renderInternal(in, page, urlOutputFilter);
  }

  // TODO: Merge back into render when main() is gone
  private static ResultNode renderInternal(ANTLRInputStream in, final PageInfo page, final URLOutputFilter urlOutputFilter) {
    CreoleTokens lexer = new CreoleTokens(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    // TODO: Remove
    parser.setTrace(true);

    ParseTree tree = parser.creole();

    // TODO: Remove
    System.out.println(tree.toStringTree(parser));

    // TODO: Pass in the link thingy properly
    ParseTreeVisitor<RenderNode> visitor = new Visitor(null);
    List<ResultNode> res;

    if (page == null) {
      // TODO: Remove this branch entirely
      res = visitor.visit(tree).render(null, null, null, null);
    }
    else {
      res = visitor.visit(tree).render(page, page.getContent(), null, urlOutputFilter);
    }

    assert (res.size() == 0);
    return res.get(0);
  }

  // TODO: Remove
  public static void main(String args[]) {
    String tests[] = {
        "Plaintext", "WikiWords",
        "**BoldNoSpaces**", "** Bold Spaces **",
        "//ItalicNoSpaces//", "// Italic Spaces //",
        "--StrikethroughNoSpaces--", "-- Strikethrough Spaces --",
        "Mixed **Bold and //Italic//**",
        "[[LinkToSomewhere]]",
        "[[Link|With a wonderful title]]",
        "{{ImageLink.png|Alt text!}}",
        "{{{inline      pre}}}",
        "{{{INLINE}}} pre",
        "inline {{{PRE}}}",
        "Line\\\\\nBreak",
        "\\\\\nBreak",
        "Break\\\\\n",
        "----", "  ----  ", "Not a ---- hrule",
        "= Heading 1", "  == Heading 2",
        "# Numbered\n#List", "#Numbered **List** with some {{{formatting}}} and ExcitingStuff",
        "#Numbered\n##List\n###With\n###Children\n##Exciting\n##Isn't\n#It?",
        "#Line\n\n\n\n#Breaks",
        "{{{Preformatted\nBlock}}}",
        "Long {{{preformated inline}}}}}}",
        "|=th", "|td", "|=th|td|\n|cell 1|**bold //italic//**"};

    for (String test : tests) {
      System.out.println("TEST: " + test);
      ResultNode res = renderInternal(new ANTLRInputStream(test), null, null);
      System.out.println(res.toXHTML());
      System.out.println();
    }
  }
}
