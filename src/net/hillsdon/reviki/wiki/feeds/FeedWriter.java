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
package net.hillsdon.reviki.wiki.feeds;

import java.io.PrintWriter;
import java.util.Collection;

import javax.xml.transform.TransformerConfigurationException;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.wiki.WikiUrls;

import org.xml.sax.SAXException;


/**
 * Converts changes to a syndication format. 
 * 
 * @author mth
 */
public interface FeedWriter {
  
  void writeAtom(WikiUrls wikiUrls, PrintWriter out, Collection<ChangeInfo> changes) throws TransformerConfigurationException, SAXException;

}
