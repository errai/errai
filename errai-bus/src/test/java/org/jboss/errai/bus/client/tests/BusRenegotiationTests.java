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

package org.jboss.errai.bus.client.tests;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.tests.support.SlowService;
import org.jboss.errai.bus.common.AbstractErraiTest;

import com.google.gwt.user.client.Timer;

/**
 * @author Mike Brock
 */
public class BusRenegotiationTests extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testBusRecoversFromSessionExpiry() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        logger.info("Starting testBusRecoversFromSessionExpiry runnable...");
        MessageBuilder.createMessage()
            .toSubject("ExpiryService")
            .signalling().noErrorHandling().sendNowWith(ErraiBus.get());

        new Timer() {
          @Override
          public void run() {
            logger.info("Running testBusRecoversFromSessionExpiry timer...");
            MessageBuilder.createMessage()
                .toSubject("TestService3")
                .signalling().noErrorHandling().repliesTo(
                new MessageCallback() {
                  @Override
                  public void callback(final Message message) {
                    finishTest();
                  }
                }
            ).sendNowWith(ErraiBus.get());
          }
        }.schedule(500);
      }
    });
  }

  public void testRpcResponseSubjectsNotAdvertisedAfterReconnection() throws Exception {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        logger.info("Calling slow service...");
        MessageBuilder.createCall(
                (final Long retVal) -> fail(
                        "RemoteCallback invoked. Likely the assertion that caused the failure was swallowed. See logs for details."),
                (m, e) -> {
                  fail("ErrorCallback invoked");
                  return false;
                }, SlowService.class).sleep(10000);

        final ClientMessageBus bus = (ClientMessageBus) ErraiBus.get();

        logger.info("Stopping bus in test...");
        bus.stop(false);

        logger.info("Reinitializing bus in test...");
        bus.init();

        logger.info("Running assertions in test...");
        final Set<String> allSubjects = bus.getAllRegisteredSubjects();
        final List<String> rpcResponseSubjects = allSubjects.stream()
                .filter(s -> s.endsWith(":RPC")).collect(Collectors.toList());
        try {
          assertEquals(Collections.emptyList(), rpcResponseSubjects);
        } catch (final AssertionError ae) {
          logger.info("Test failed.", ae);
          throw ae;
        }
        logger.info("Test completed");
        finishTest();
      }
    });
  }
}
