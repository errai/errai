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

package org.jboss.errai.bus.server.websocket.jsr356.weld.session;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.server.websocket.jsr356.weld.SyncBeanStore;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.servlet.SessionHolder;

/**
 * Adapter for {@link javax.enterprise.context.SessionScoped}
 * 
 * @author Michel Werren
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class WeldSessionScopeAdapter implements SessionScopeAdapter {

  private static final String BEAN_STORE_SESSION_ATTR_NAME = "erraiBeanStore";
  private static WeldSessionScopeAdapter instance;
  private final BeanManagerImpl beanManager;
  
  private static ThreadLocal<HttpServletRequest> simulatedHttpRequest = new ThreadLocal<HttpServletRequest>(); 

  private WeldSessionScopeAdapter(BeanManagerImpl beanManager) {
    this.beanManager = beanManager;
  }

  public static WeldSessionScopeAdapter getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Adapter not initialized!");
    }
    return instance;
  }

  public static void init(BeanManagerImpl beanManager) {
    if (instance == null) {
      instance = new WeldSessionScopeAdapter(beanManager);
    }
  }

  @Override
  public void activateContext(HttpSession httpSession) {
    HttpSessionContext sessionContext = beanManager.instance().select(HttpSessionContext.class).get();
    simulatedHttpRequest.set(new FakeHttpServletRequest(httpSession));
    SessionHolder.requestInitialized(simulatedHttpRequest.get());

    sessionContext.associate(simulatedHttpRequest.get());
    sessionContext.activate();
    getOrCreateBeanStore(httpSession);
  }

  @Override
  public void activateContext() {
    throw new RuntimeException(new OperationNotSupportedException("Session scope must be referenced with HTTP session"));
  }

  @Override
  public void invalidateContext() {
    deactivateContext();
  }

  @Override
  public void deactivateContext() {
    HttpSessionContext sessionContext = beanManager.instance().select(HttpSessionContext.class).get();
    SessionHolder.clear();
    sessionContext.dissociate(simulatedHttpRequest.get());
  }
  
  private SyncBeanStore getOrCreateBeanStore(HttpSession httpSession) {
    final Object beanStore = httpSession.getAttribute(BEAN_STORE_SESSION_ATTR_NAME);
    if (beanStore != null) {
      return (SyncBeanStore) beanStore;
    }
    else {
      final SyncBeanStore newBeanStore = new SyncBeanStore();
      httpSession.setAttribute(BEAN_STORE_SESSION_ATTR_NAME, newBeanStore);
      return newBeanStore;
    }
  }
   
  public static SyncBeanStore getCurrentBeanStore() {
    if (simulatedHttpRequest.get() == null) 
      throw new RuntimeException("No HTTP session associated!");
    
    return (SyncBeanStore) simulatedHttpRequest.get().getSession().getAttribute(BEAN_STORE_SESSION_ATTR_NAME);
  }
}
