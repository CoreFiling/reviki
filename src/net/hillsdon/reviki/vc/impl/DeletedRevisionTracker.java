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

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeSubscriber;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;

/**
 * SVN provides no way to do svn log or similar on a delete URL
 * without knowing the last URL at which it existed.  They
 * recommend using svn log -v on the parent directory to find
 * that revision.  On a wiki with many revisions this is intolerably
 * slow as all the action is in a single directory.
 * 
 * We therefore keep up to date by reading the svn log an remember
 * revisions in which files were deleted.
 *
 * @author mth
 *
 */
public interface DeletedRevisionTracker extends ChangeSubscriber {

  ChangeInfo getChangeThatDeleted(BasicSVNOperations helper, String path) throws PageStoreAuthenticationException, PageStoreException;

}
