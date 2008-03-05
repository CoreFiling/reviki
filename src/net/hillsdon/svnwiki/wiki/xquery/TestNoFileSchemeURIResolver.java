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
package net.hillsdon.svnwiki.wiki.xquery;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

public class TestNoFileSchemeURIResolver extends TestCase {

  private static final String[][] INVALID = new String[][] {
      new String[] {"file:/etc/passwd", null},
      new String[] {"", "file:/etc/passwd"},
      new String[] {"etc/password", "file:/"},
    };

  private static final String[][] VALID = new String[][] {
    new String[] {"http://www.example.com/etc/passwd", null},
    new String[] {"", "http://www.example.com/etc/passwd"},
    new String[] {"etc/password", "http://www.example.com/"},
  };
  
  public void testInvalidThrowsException() {
    for (String[] parts : INVALID) {
      try {
        new NoFileSchemeURIResolver().resolve(parts[0], parts[1]);
        fail();
      }
      catch (TransformerException expected) {
      }
    }
  }

  public void testValidReturnsNull() throws Exception {
    for (String[] parts : VALID) {
      new NoFileSchemeURIResolver().resolve(parts[0], parts[1]);
    }
  }
  
}
