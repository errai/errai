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

package org.jboss.errai.enterprise.client.cdi;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.Container;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Abstract base class of all Errai CDI integration tests,
 * used to bootstrap our IOC container and CDI module.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractErraiCDITest extends GWTTestCase {

  @Override
  public void gwtSetUp() throws Exception {
    InitVotes.reset();
    InitVotes.setTimeoutMillis(60000);
    super.gwtSetUp();

    CDI.removePostInitTasks();

    // Unfortunately, GWTTestCase does not call our inherited module's onModuleLoad() methods
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3791
    new Container().onModuleLoad();
    new CDIClientBootstrap().onModuleLoad();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
  }
}