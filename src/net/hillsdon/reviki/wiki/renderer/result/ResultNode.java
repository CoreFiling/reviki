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

import net.hillsdon.reviki.wiki.renderer.context.PageRenderContext;

import java.util.List;

/**
 * We encode the results of parsing wiki mark-up as a ResultNode tree.
 * 
 * The idea is we can encode useful information for analysis and
 * transformation but for now most of the nodes are generic and the
 * rendering work is still done in by the 
 * {@link net.hillsdon.reviki.wiki.renderer.creole.RenderNode}s.
 * 
 * @author mth
 */
public interface ResultNode {

  List<ResultNode> getChildren();
  
  String toXHTML();

  PageRenderContext getContext();

}
