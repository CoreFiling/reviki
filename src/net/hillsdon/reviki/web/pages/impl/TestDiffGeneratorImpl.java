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
