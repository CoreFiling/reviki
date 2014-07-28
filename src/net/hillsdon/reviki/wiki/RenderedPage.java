/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.wiki;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTVisitor;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Link;

public class RenderedPage {

  private static final String NEW_PAGE_CLASS = "new-page";

  private static final String EXIST_PAGE_CLASS = "existing-page";

  private final String _pageName;

  private final ASTNode _ast;

  public RenderedPage(final String pageName, final ASTNode resultNode) {
    _pageName = pageName;
    _ast = resultNode;
  }

  public String getPage() {
    return _pageName;
  }

  /**
   * @return outgoing links in document order.
   */
  public List<String> findOutgoingWikiLinks() {
    return (new Visitor()).visit(_ast);
  }

  private final class Visitor extends ASTVisitor<List<String>> {
    @Override
    public List<String> visitASTNode(ASTNode node) {
      List<String> outgoing = new ArrayList<String>();

      for (ASTNode child : node.getChildren()) {
        outgoing.addAll(visit(child));
      }

      return outgoing;
    }

    @Override
    public List<String> visitLink(Link node) {
      try {
        String style = node.getParts().getStyleClass(node.getContext());
        String href = node.getParts().getURL(node.getContext());

        if (style.equals(NEW_PAGE_CLASS) || style.equals(EXIST_PAGE_CLASS)) {
          return ImmutableList.of(href.substring(href.lastIndexOf('/') + 1));
        }
      }
      catch (Exception e) {
        // Ignore the bad link, we only care about links to pages on this wiki.
      }

      return ImmutableList.of();
    }
  }
}
