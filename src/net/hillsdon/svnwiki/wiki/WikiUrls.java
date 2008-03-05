package net.hillsdon.svnwiki.wiki;

/**
 * Wikis are rather tied up with the web.  At the moment we assume the public URL
 * is the servlet container URL which is probably dubious if apache is fronting
 * tomcat etc.  Probably need a configurable base URL.
 * 
 * These methods return fully qualified URLs.
 * 
 * @author mth
 */
public interface WikiUrls {

  String root();
  
  String search();
  
  String page(String name);

  String feed();
  
}
