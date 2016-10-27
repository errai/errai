/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.support.bus.tests.client.res.CallerBean;
import org.jboss.errai.ioc.support.bus.tests.client.res.Greeter;
import org.jboss.errai.ioc.support.bus.tests.client.res.OfflineMessageCallback;

/**
 * Tests shadowed RPC services.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ShadowServiceIntegrationTest extends AbstractErraiIOCBusTest {

  public void testCallerUsesShadowServiceIfRemoteEndpointDoesntExist() {
    runAfterInit(() -> {
      final Greeter greeter = IOC.getBeanManager().lookupBean(Greeter.class).getInstance();
      final CallerBean callerBean = IOC.getBeanManager().lookupBean(CallerBean.class).getInstance();
      callerBean.getOfflineServiceCaller().call((r) -> {
        assertEquals(greeter.offline(), r);
        finishTest();
      }).greeting("foo");
    });
  }

  public void testCallerUsesShadowServiceIfBusNotConnected() {
    runAfterInit(() -> {
      ((ClientMessageBusImpl) ErraiBus.get()).setState(BusState.CONNECTION_INTERRUPTED);

      final Greeter greeter = IOC.getBeanManager().lookupBean(Greeter.class).getInstance();
      final CallerBean callerBean = IOC.getBeanManager().lookupBean(CallerBean.class).getInstance();
      callerBean.getOnlineServiceCaller().call((r) -> {
        assertEquals(greeter.offline(), r);
        finishTest();
      }).greeting("foo");
    });
  }

  public void testCallerUsesRemoteEndpointIfBusConnected() {
    runAfterInit(() -> {
      final Greeter greeter = IOC.getBeanManager().lookupBean(Greeter.class).getInstance();
      final CallerBean callerBean = IOC.getBeanManager().lookupBean(CallerBean.class).getInstance();
      callerBean.getOnlineServiceCaller().call((r) -> {
        assertEquals(greeter.online(), r);
        finishTest();
      }).greeting("foo");
    });
  }

  public void testShadowServiceAsMessageCallback() {
    runAfterInit(() -> {
      final OfflineMessageCallback callback = IOC.getBeanManager().lookupBean(OfflineMessageCallback.class).getInstance();
      assertNull(callback.getGreeting());

      MessageBuilder.createMessage()
        .toSubject("Greeting")
        .signalling()
        .with("greeting", "Hello, there")
        .noErrorHandling()
        .sendNowWith(ErraiBus.get());

      assertNotNull(callback.getGreeting());
      assertEquals("Hello, there", callback.getGreeting());
      finishTest();
    });
  }

}
