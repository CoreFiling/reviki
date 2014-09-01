package net.hillsdon.reviki.configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class ConfigureSearchIndex implements ServletContextListener {

  private SearchIndexBuilder _indexBuilder;
  private ServletContext _servletContext;


  public void contextDestroyed(ServletContextEvent event) {
    _indexBuilder.stop();

  }

  public void contextInitialized(ServletContextEvent event) {
    _servletContext = event.getServletContext();
    _indexBuilder = new SearchIndexBuilder(_servletContext);
    _indexBuilder.start();
  }

}
