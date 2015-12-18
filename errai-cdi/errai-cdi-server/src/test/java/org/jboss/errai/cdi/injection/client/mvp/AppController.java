/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.injection.client.mvp;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import org.jboss.errai.ioc.client.container.ClientBeanManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class AppController {
  @Inject
  private ClientBeanManager manager;

  @Inject
  private HandlerManager eventBus;

  private HasWidgets container;
  
  // This bean is proxied, we add some methods to it that should not be overridden by the proxy for testing purposes
  public static void staticMethod() {}
  private void privateMethod() {}
  void packagePrivateMethod() {}

  public void go(HasWidgets container) {
    this.container = container;
  }

  public ClientBeanManager getManager() throws Exception {
    return manager;
  }

  public HandlerManager getEventBus() {
    return eventBus;
  }

  public HasWidgets getContainer() {
    return container;
  }
}
