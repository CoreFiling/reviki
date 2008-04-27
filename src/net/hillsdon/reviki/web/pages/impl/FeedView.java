package net.hillsdon.reviki.web.pages.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;

public class FeedView implements View {
  
  private final FeedWriter _feedWriter;
  private final List<ChangeInfo> _changes;

  public FeedView(final FeedWriter feedWriter, final List<ChangeInfo> changes) {
    _feedWriter = feedWriter;
    _changes = changes;
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    response.setContentType("application/atom+xml");
    _feedWriter.writeAtom(_changes, response.getWriter());
  }
  
}