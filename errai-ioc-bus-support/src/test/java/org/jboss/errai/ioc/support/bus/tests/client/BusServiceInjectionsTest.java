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

package org.jboss.errai.ioc.support.bus.tests.client;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.support.bus.tests.client.res.SimpleBean;

/**
 * @author Mike Brock
 */
public class BusServiceInjectionsTest extends AbstractErraiIOCBusTest {

  public void testBusGetsInjected() {
    SimpleBean simpleBean = IOC.getBeanManager().lookupBean(SimpleBean.class).getInstance();
    assertNotNull(simpleBean);

    final MessageBus expected = ErraiBus.get();
    assertEquals(expected, simpleBean.getBus());
    assertEquals(expected, simpleBean.getBus2());
    assertEquals(expected, simpleBean.getBus3());
    assertEquals(expected, simpleBean.getBus4());
    assertEquals(expected, simpleBean.getClientMessageBus());

    assertNotNull(simpleBean.getDispatcher());
    assertNotNull(simpleBean.getDispatcher2());
    assertNotNull(simpleBean.getDispatcher3());
    assertNotNull(simpleBean.getDispatcher4());
  }
}
