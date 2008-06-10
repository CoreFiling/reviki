package net.hillsdon.reviki.vc.impl;

import junit.framework.TestCase;

import org.tmatesoft.svn.core.internal.util.SVNDate;

/**
 * Part of investigation into SVN date handling, to be delete when
 * bug is fixed as I don't think it's svnkit's fault.
 * 
 * @author mth
 */
public class TestSVNDateParsing extends TestCase {

  public void test() throws Exception {
    String asSentBySvn = "2008-06-10T20:35:18.842496Z";
    SVNDate.parseDatestamp(asSentBySvn);
  }

}
