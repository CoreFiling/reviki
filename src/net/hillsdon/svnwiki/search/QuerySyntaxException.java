package net.hillsdon.svnwiki.search;

/**
 * Thrown if a search query wasn't valid Lucene syntax.
 *  
 * @author mth
 */
public class QuerySyntaxException extends Exception {
  private static final long serialVersionUID = 1L;
  public QuerySyntaxException(String message, Throwable cause) {
    super(message, cause);
  }
}
