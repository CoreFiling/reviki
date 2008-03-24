package net.hillsdon.reviki.vc.impl;

import java.util.Comparator;

import net.hillsdon.reviki.vc.ChangeInfo;

/**
 * This makes sense as there are often delete / create pairings for rename.
 * 
 * @author mth
 */
public class DeletesAfterOtherSameRevisionChanges implements Comparator<ChangeInfo> {
  
  public static final Comparator<ChangeInfo> INSTANCE = new DeletesAfterOtherSameRevisionChanges();

  /**
   * @see #INSTANCE 
   */
  private DeletesAfterOtherSameRevisionChanges() {
  }
  
  public int compare(final ChangeInfo o1, final ChangeInfo o2) {
    if (o1.getRevision() == o2.getRevision()) {
      return (o2.isDeletion() ? 0 : 1) - (o1.isDeletion() ? 0 : 1);
    }
    return 0;
  }
  
}
