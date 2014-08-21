package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * Abstract node type for things rendered using link handlers.
 *
 * @author msw
 */
public abstract class LinkNode extends ASTNode {
  private final LinkPartsHandler _handler;

  private final PageInfo _page;

  private final LinkParts _parts;

  private final String _title;

  private final String _target;

  public LinkNode(final String target, final String title, final PageInfo page, final LinkPartsHandler handler) {
    _title = title;
    _target = target;
    _parts = CreoleLinkContentsSplitter.split(target, title);
    _page = page;
    _handler = handler;
  }

  /**
   * Get the handler.
   */
  public LinkPartsHandler getHandler() {
    return _handler;
  }

  /**
   * Get the page.
   */
  public PageInfo getPage() {
    return _page;
  }

  /**
   * Get the split link.
   */
  public LinkParts getParts() {
    return _parts;
  }

  /**
   * Get the resolution context.
   */
  public LinkResolutionContext getContext() {
    return _handler.getContext();
  }

  /**
   * Get the title.
   */
  public String getTitle() {
    return _title;
  }

  /**
   * Get the target.
   */
  public String getTarget() {
    return _target;
  }

  @Override
  public List<ASTNode> expandMacrosInt(Supplier<List<Macro>> macros) {
    return ImmutableList.of((ASTNode) this);
  }
}
