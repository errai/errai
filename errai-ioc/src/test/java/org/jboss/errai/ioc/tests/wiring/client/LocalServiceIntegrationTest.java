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

package org.jboss.errai.ioc.tests.wiring.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.annotations.Local;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.Container;

/**
 * @author Mike Brock
 */
public class LocalServiceIntegrationTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new Container().onModuleLoad();
  }

  @Service
  @Local
  public static class LocalTestCompleteService implements MessageCallback {
    static boolean received = false;

    @Override
    public void callback(Message message) {
      received = true;
    }
  }

  @Service
  public static class LocalTestCompleteServiceConfirmation implements MessageCallback {
    static boolean worked = false;

    @Override
    public void callback(Message message) {
      worked = true;
    }
  }

  public void testLocalSemanticsOnSubject() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        MessageBuilder.createMessage()
                .toSubject("LocalTestTesterService")
                .done().sendNowWith(ErraiBus.getDispatcher());

        new Timer() {
          @Override
          public void run() {
            if (LocalTestCompleteServiceConfirmation.worked) {
              finishTest();
            }
            else if (LocalTestCompleteService.received) {
              fail("received message from server!");
            }
            else {
              schedule(100);
            }
          }

        }.schedule(100);
      }
    });
  }

  public void testLocalSubjectCallable() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {

        MessageBuilder.createMessage()
                .toSubject("LocalTestCompleteService")
                .done().sendNowWith(ErraiBus.getDispatcher());

        new Timer() {
          @Override
          public void run() {
            if (LocalTestCompleteService.received) {
              finishTest();
            }
            else {
              schedule(100);
            }
          }

        }.schedule(100);
      }
    });
  }
}
