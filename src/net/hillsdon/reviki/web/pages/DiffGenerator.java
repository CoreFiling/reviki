package net.hillsdon.reviki.web.pages;


/**
 * Generates XHTML diffs.
 * 
 * @author mth
 */
public interface DiffGenerator {

  String getDiffMarkup(String base, String head);
  
}
