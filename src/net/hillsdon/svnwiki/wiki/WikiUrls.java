package net.hillsdon.svnwiki.wiki;

/**
 * Wikis are rather tied up with the web.
 * 
 * These methods take and return fully qualified URLs.
 * 
 * @author mth
 */
public interface WikiUrls {

  String root();
  
  String search();
  
  String page(String name);

  boolean isPage(String url);

  String attachment(String page, String name);
  
  boolean isAttachmentsDir(String url);
  
  boolean isAttachment(String url);

  String feed();
  
}
