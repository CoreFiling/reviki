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
package net.hillsdon.reviki.web.urls;


public interface ApplicationUrls {

  /**
   * Prefer adding methods to using this one.
   * 
   * @param relative With leading '/'.
   * @return An absolute URL within this application.
   */
  String url(String relative);
  
  /**
   * @return URL for the wiki list.
   */
  String list();
  
  /**
   * Note the returned value may be specific to the current request.
   * 
   * @param name The wiki name (null for default).
   * @return The relevant URLs.
   */
  WikiUrls get(String name);
  
}
