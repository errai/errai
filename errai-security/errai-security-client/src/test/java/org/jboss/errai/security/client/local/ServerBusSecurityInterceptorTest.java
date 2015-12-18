/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.*;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.security.client.local.res.Counter;
import org.jboss.errai.security.client.local.res.CountingMessageCallback;
import org.jboss.errai.security.shared.api.identity.User;

public class ServerBusSecurityInterceptorTest extends BusSecurityInterceptorTest {

  private int counter;
  private Subscription errorSubscription;

  @Override
  protected void postLogout() {
    provider.invalidateCache();
  }

  @Override
  protected void postLogin(final User user) {
    provider.invalidateCache();
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    counter = 0;
    errorSubscription = ErraiBus.get().subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
      @Override
      public void callback(Message message) {
        final Throwable throwable = message.get(Throwable.class, MessageParts.Throwable);
        if (throwable instanceof org.jboss.errai.security.shared.exception.SecurityException) {
          counter++;
        }
      }
    });
  }

  @Override
  protected void gwtTearDown() throws Exception {
    errorSubscription.remove();
    super.gwtTearDown();
  }

  public void testSecureCallbackNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        createMessage("SecureMessageCallback").signalling().defaultErrorHandling()
                .repliesTo(new CountingMessageCallback(counter)).sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, ServerBusSecurityInterceptorTest.this.counter);
          }
        });
      }
    });
  }

  public void testSecureClassNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, ServerBusSecurityInterceptorTest.this.counter);
          }
        });

        createMessage("methodInSecureClass").signalling().defaultErrorHandling()
                .repliesTo(new CountingMessageCallback(counter)).sendNowWith(ErraiBus.get());
      }
    });
  }

  public void testSecureMethodNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, ServerBusSecurityInterceptorTest.this.counter);
          }
        });

        createMessage("secureMethod").signalling().defaultErrorHandling()
                .repliesTo(new CountingMessageCallback(counter)).sendNowWith(ErraiBus.get());
      }
    });
  }

  public void testInsecureMethodInClassWithSecureMethodNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, counter.getCount());
          }
        });

        createMessage("insecureMethod").signalling().defaultErrorHandling()
                .repliesTo(new CountingMessageCallback(counter)).sendNowWith(ErraiBus.get());
      }
    });
  }

  public void testCommandMethodInSecureClassNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, ServerBusSecurityInterceptorTest.this.counter);
          }
        });

        createMessage("commandMethodInSecureClass").command("command").defaultErrorHandling()
                .repliesTo(new CountingMessageCallback(counter)).sendNowWith(ErraiBus.get());
      }
    });
  }

  public void testSecureCommandMethodNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        createMessage("secureCommandMethod").command("command").defaultErrorHandling()
                .repliesTo(new CountingMessageCallback(counter)).sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, ServerBusSecurityInterceptorTest.this.counter);
          }
        });
      }
    });
  }
}
