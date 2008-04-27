package net.hillsdon.reviki.di.impl;

import javax.servlet.ServletContext;

import net.hillsdon.reviki.configuration.ApplicationUrls;
import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.PropertiesDeploymentConfiguration;
import net.hillsdon.reviki.configuration.RequestScopedApplicationUrls;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.di.ApplicationSession;
import net.hillsdon.reviki.di.WikiSession;
import net.hillsdon.reviki.web.dispatching.Dispatcher;
import net.hillsdon.reviki.web.dispatching.impl.DispatcherImpl;
import net.hillsdon.reviki.web.dispatching.impl.WikiChoiceImpl;
import net.hillsdon.reviki.web.handlers.JumpToWikiUrl;
import net.hillsdon.reviki.web.handlers.ListWikis;
import net.hillsdon.reviki.web.handlers.impl.JumpToWikiUrlImpl;
import net.hillsdon.reviki.web.handlers.impl.ListWikisImpl;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAwareManager;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAwareManagerImpl;

import org.picocontainer.MutablePicoContainer;

public class ApplicationSessionImpl extends AbstractSession implements ApplicationSession {

  public ApplicationSessionImpl(final ServletContext servletContext) {
    super(null /* we're the top level session */, servletContext);
  }

  public WikiSession createWikiSession(final WikiConfiguration configuration) {
    return new WikiSessionImpl(this, configuration);
  }

  public Dispatcher getDispatcher() {
    return getContainer().getComponent(Dispatcher.class);
  }

  public void configure(final MutablePicoContainer container) {
    container.addComponent(ApplicationSession.class, this);
    container.addComponent(Dispatcher.class, DispatcherImpl.class);
    container.addComponent(DeploymentConfiguration.class, PropertiesDeploymentConfiguration.class);
    container.addComponent(ListWikis.class, ListWikisImpl.class);
    container.addComponent(JumpToWikiUrl.class, JumpToWikiUrlImpl.class);
    container.addComponent(WikiChoiceImpl.class);
    container.addComponent(RequestLifecycleAwareManager.class, RequestLifecycleAwareManagerImpl.class);
    container.addComponent(ApplicationUrls.class, RequestScopedApplicationUrls.class);
  }

}
