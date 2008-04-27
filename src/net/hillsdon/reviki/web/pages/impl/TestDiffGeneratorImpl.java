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
package net.hillsdon.reviki.web.pages.impl;

import junit.framework.TestCase;
import net.hillsdon.reviki.web.pages.DiffGenerator;

public class TestDiffGeneratorImpl extends TestCase {

  private DiffGenerator _diffGenerator = new DiffGeneratorImpl();
  
  public void testXHTMLMarkup() {
    String expected = "<span class='diff'>Happy </span><del class='diff'>day</del><ins class='diff'>hour</ins><span class='diff'>s are<br /><br />here </span><del class='diff'>again</del><ins class='diff'>today</ins><span class='diff'>!</span>";
    String actual = _diffGenerator.getDiffMarkup("Happy days are\n\nhere again!", "Happy hours are\n\nhere today!");
    assertEquals(expected, actual);
  }
  
}
