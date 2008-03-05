package net.hillsdon.svnwiki.wiki;

/**
 * Thrown if we're asked to produce an inter-wiki link for a wiki
 * we don't know about.
 * 
 * @author mth
 */
public class UnknownWikiException extends Exception {
  private static final long serialVersionUID = 1L;
}
