package net.hillsdon.reviki.web.dispatching.impl;

import junit.framework.TestCase;

public class TestWikiChoiceImpl extends TestCase {

  private WikiChoiceImpl _choice;

  @Override
  public void setUp() {
    _choice = new WikiChoiceImpl(null, null, null);
  }

  public void testNullIsInvalidWikiName() {
    assertFalse(_choice.isValidWikiName(null));
  }

  public void testEmptyStringIsInvalidWikiName() {
    assertFalse(_choice.isValidWikiName(""));
  }

  public void testNonLowerInitialIsInvalidWikiName() {
    assertFalse(_choice.isValidWikiName("FooPage"));
  }

  public void testValidWikiNames() {
    assertTrue(_choice.isValidWikiName("fooPage"));
    assertTrue(_choice.isValidWikiName("foo"));
  }

}
