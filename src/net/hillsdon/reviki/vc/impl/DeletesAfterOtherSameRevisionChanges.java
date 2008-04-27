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
