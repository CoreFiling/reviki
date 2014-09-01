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

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageReference;

public class TestPageReferenceImpl extends TestCase {

  private static final String PATH = "foo/bar/BlortPage";
  private static final PageReference REFERENCE = new PageReferenceImpl(PATH);
  
  public void testPathIsAsGiven() {
    assertEquals(PATH, REFERENCE.getPath());
  }
  
  public void testNameBasedOnPath() {
    assertEquals("BlortPage", REFERENCE.getName());
  }
  
  public void testTitleBasedOnName() {
    assertEquals("Blort Page", REFERENCE.getTitle());
  }
  
}
