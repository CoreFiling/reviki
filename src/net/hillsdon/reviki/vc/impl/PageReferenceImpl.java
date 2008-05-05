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

import net.hillsdon.reviki.text.WikiWordUtils;
import net.hillsdon.reviki.vc.PageReference;

import org.tmatesoft.svn.core.internal.util.SVNPathUtil;

/**
 * Impl.
 * 
 * @author mth
 */
public class PageReferenceImpl implements PageReference {

  private final String _path;

  public PageReferenceImpl(final String path) {
    _path = path;
  }

  public final String getTitle() {
    return WikiWordUtils.pathToTitle(getName());
  }
  
  public final String getPath() {
    return _path;
  }

  public final int compareTo(final PageReference o) {
    return _path.compareTo(o.getPath());
  }

  @Override
  public final String toString() {
    return _path;
  }

  @Override
  public final boolean equals(final Object obj) {
    if (obj instanceof PageReference) {
      return ((PageReference) obj).getPath().equals(_path);
    }
    return false;
  }
  
  @Override
  public final int hashCode() {
    return _path.hashCode();
  }

  public final String getName() {
    return SVNPathUtil.tail(_path);
  }

}

