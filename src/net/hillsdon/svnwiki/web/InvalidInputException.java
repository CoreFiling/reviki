package net.hillsdon.svnwiki.web;

public class InvalidInputException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidInputException(final String message) {
    super(message);
  }
  
}
