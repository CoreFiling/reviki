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
package net.hillsdon.reviki.wiki.renderer.result;

import java.util.Collections;
import java.util.List;


/**
 * Outputs an XHTML tag.
 * 
 * @author mth
 */
public class TagResultNode extends CompositeResultNode {
  
  public static final String CSS_CLASS_ATTR = "class='wiki-content'";
  
  private final String _tag;

  public TagResultNode(final String tag) {
    this(tag, Collections.<ResultNode>emptyList());
  }

  public TagResultNode(final String tag, final List<ResultNode> children) {
    super(children);
    _tag = tag;
  }
  
  public String getTag() {
    return _tag;
  }
  
  public String toXHTML() {
    if (getChildren().isEmpty()) {
      return "<" + _tag + " " + CSS_CLASS_ATTR + " />";
    }
    return "<" + _tag + " " + CSS_CLASS_ATTR + ">" +  super.toXHTML() + "</" + _tag + ">";
  }

}
