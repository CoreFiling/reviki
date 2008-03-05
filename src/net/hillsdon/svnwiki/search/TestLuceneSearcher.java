package net.hillsdon.svnwiki.search;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

public class TestLuceneSearcher extends TestCase {

  /**
   * It isn't a valid query but its daft to choke on it.
   */
  public void testTrimToEmptyStringNoResults() throws Exception {
    assertEquals(Collections.emptySet(), new LuceneSearcher(new File("nowhere")).search("  "));
  }
  
}
