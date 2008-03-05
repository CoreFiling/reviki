package net.hillsdon.svnwiki.search;


public class QuerySyntaxException extends Exception {
  private static final long serialVersionUID = 1L;
  public QuerySyntaxException(String message, Throwable cause) {
    super(message, cause);
  }
}
