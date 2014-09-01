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
package net.hillsdon.reviki.di;

import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.dispatching.Dispatcher;

/**
 * One per deployment.  Has N child WikiSessions which have most
 * of the interesting objects.
 * 
 * @author mth
 */
public interface ApplicationSession extends Session {
  
  /**
   * @return The global web dispatcher.
   */
  Dispatcher getDispatcher();
  
  /**
   * @param configuration The wiki specific configuration.
   * @return A new session for that wiki.
   */
  WikiSession createWikiSession(WikiConfiguration configuration);
  
}