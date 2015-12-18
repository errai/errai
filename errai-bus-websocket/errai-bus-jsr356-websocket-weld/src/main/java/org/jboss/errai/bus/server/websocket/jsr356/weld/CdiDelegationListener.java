/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.websocket.jsr356.weld;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.weld.channel.CdiErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.WeldConversationScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.filter.CdiFilterLookup;
import org.jboss.errai.bus.server.websocket.jsr356.weld.request.WeldRequestScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.session.WeldSessionScopeAdapter;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michel Werren
 */
@WebListener
public class CdiDelegationListener implements ServletContextListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(CdiDelegationListener.class.getName());

  @Inject
  private BoundRequestContext boundRequestContext;

  @Inject
  private BoundConversationContext boundConversationContext;

  @Inject
  private CdiFilterLookup cdiFilterLookup;
  
  @Inject
  private BeanManagerImpl beanManager;
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    
    WeldConversationScopeAdapter.init(boundConversationContext);
    WeldSessionScopeAdapter.init(beanManager);
    WeldRequestScopeAdapter.init(boundRequestContext);

    ErraiChannelFactory.registerDelegate(CdiErraiChannelFactory.getInstance());
    FilterLookup.registerDelegate(cdiFilterLookup);

    LOGGER.info("CDI activated for Errai Bus JSR-356 websocket");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }
}
