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

public class TestFixedMimeIdentifier extends TestCase {

  public void test() {
    FixedMimeIdentifier mimeIdentifier = new FixedMimeIdentifier();
    assertTrue(mimeIdentifier.isImage("foo.png"));
    assertFalse(mimeIdentifier.isImage("foo.doc"));
    assertFalse(mimeIdentifier.isImage(".doc"));
    assertFalse(mimeIdentifier.isImage("."));
    assertFalse(mimeIdentifier.isImage("foo."));
  }
  
}
