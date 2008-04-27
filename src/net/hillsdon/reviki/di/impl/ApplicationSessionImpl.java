/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.di.impl;

import javax.servlet.ServletContext;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.PropertiesDeploymentConfiguration;
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
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.impl.RequestScopedApplicationUrls;
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
