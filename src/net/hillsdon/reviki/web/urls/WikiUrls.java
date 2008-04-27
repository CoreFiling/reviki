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

/**
 * Wikis are rather tied up with the web.  At the moment we assume the public URL
 * is the servlet container URL which is probably dubious if apache is fronting
 * tomcat etc.  Probably need a configurable base URL.
 * 
 * These methods return fully qualified URLs.
 * 
 * @author mth
 */
public interface WikiUrls {

  String root();
  
  String search();
  
  String page(String name);

  String feed();

  String favicon();
  
}
