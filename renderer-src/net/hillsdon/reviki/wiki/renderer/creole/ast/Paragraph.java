package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class Paragraph extends ASTNode {
  public Paragraph(final ASTNode body) {
    super(body);

    _isBlock = true;
  }

  /**
   * Paragraphs can merge with other paragraphs.
   */
  @Override
  protected boolean canMergeWith(ASTNode node) {
    return node instanceof Paragraph;
  }

  /**
   * Extract the children of another paragraph.
   */
  @Override
  protected List<ASTNode> mergeChildren(List<ASTNode> children, ASTNode node) {
    Paragraph para = (Paragraph) node;
    Inline myinner = (Inline) children.get(0);
    Inline parainner = (Inline) para.getChildren().get(0);

    List<ASTNode> out = new ArrayList<ASTNode>();
    out.addAll(myinner.getChildren());
    out.addAll(parainner.getChildren());

    return ImmutableList.of((ASTNode) new Inline(out));
  }
}
