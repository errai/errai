/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.client.test;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
import org.jboss.errai.ioc.client.container.SyncBeanManagerImpl;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author Mike Brock
 */
public abstract class AbstractErraiIOCTest extends GWTTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new IOCBeanManagerLifecycle().resetBeanManager();
    new Container().bootstrapContainer();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    ((SyncBeanManagerImpl) IOC.getBeanManager()).reset();
    Container.reset();
  }

  protected void $(Runnable runnable) {
    delayTestFinish(30000);
    Container.$(runnable);
  }
}

