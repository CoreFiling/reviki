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
package net.hillsdon.reviki.search;

/**
 * Thrown if a search query wasn't valid Lucene syntax.
 *  
 * @author mth
 */
public class QuerySyntaxException extends Exception {
  private static final long serialVersionUID = 1L;
  public QuerySyntaxException(String message, Throwable cause) {
    super(message, cause);
  }
}
