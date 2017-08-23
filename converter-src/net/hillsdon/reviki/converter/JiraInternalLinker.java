package net.hillsdon.reviki.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.SimpleWikiUrls;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public class JiraInternalLinker extends InternalLinker {

  private static final String JIRA_PATH = "https://jira.int.corefiling.com";
  private static final Pattern JIRA_LINK = Pattern.compile("[A-Z]{2,}-[0-9]+");

  private final String _userUrlBase = JIRA_PATH + "/secure/ViewProfile.jspa?name=";

  public JiraInternalLinker() {
    super(SimpleWikiUrls.RELATIVE_TO.apply(JIRA_PATH + "/browse"));
  }

  @Override
  public URI uri(final String pageName) throws UnknownWikiException, URISyntaxException {
    if (pageName.startsWith("~")) {
      return new URI(_userUrlBase + pageName.substring(1));
    }
    else if (JIRA_LINK.matcher(pageName).matches()) {
      return super.uri(pageName);
    }
    else {
      return null;
    }
  }

}
