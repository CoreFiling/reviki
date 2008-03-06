/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.plugins.xquery;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Empty implementations.
 * 
 * @author mth
 */
public final class NullErrorListener implements ErrorListener {
  
  public void error(final TransformerException ignored) throws TransformerException {
  }
  public void fatalError(final TransformerException ignored) throws TransformerException {
  }
  public void warning(final TransformerException ignored) throws TransformerException {
  }
  
}
