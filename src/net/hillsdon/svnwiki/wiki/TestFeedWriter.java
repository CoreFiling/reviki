package net.hillsdon.svnwiki.wiki;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.web.RequestBasedWikiUrls;

import org.xml.sax.SAXException;

public class TestFeedWriter extends TestCase {

  public void test() throws TransformerConfigurationException, SAXException {
    StringWriter out = new StringWriter();
    List<ChangeInfo> changes = Arrays.asList(new ChangeInfo("SomeWikiPage", "mth", new Date(0), 123, "Change description"));
    FeedWriter.writeAtom(new RequestBasedWikiUrls("http://www.example.com/svnwiki"), new PrintWriter(out), changes);
    System.err.println(out);
  }
  
}
