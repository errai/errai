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

package org.jboss.errai.bus.client.tests;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.tests.support.ErrorThrowingRPCService;
import org.jboss.errai.bus.client.tests.support.SimpleRPCService;
import org.jboss.errai.bus.common.AbstractErraiTest;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.protocols.MessageParts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

/**
 * Error handling tests
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ErrorHandlingTest extends AbstractErraiTest {

  private static final String LOCAL_ERROR_THROWING_SERVICE = "LocalErrorThrowingService";

  private static class Ref<T> {
    T value;
    public Ref(T initial) {
      this.value = initial;
    }
  }

  private final Collection<Subscription> subscriptions = new ArrayList<>();

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    GWT.setUncaughtExceptionHandler(e -> {});
    super.gwtSetUp();
    subscriptions.add(bus.subscribe(LOCAL_ERROR_THROWING_SERVICE, m -> {
      throw new RuntimeException("Thrown by " + LOCAL_ERROR_THROWING_SERVICE);
    }));
  }

  @Override
  protected void gwtTearDown() throws Exception {
    for (final Subscription sub : subscriptions) {
      sub.remove();
    }
    subscriptions.clear();
    super.gwtTearDown();
  }

  public void testRemoteDefaultErrorHandling() {
    final String subject = "ErrorThrowingService";
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        final Throwable thrown = message.get(Throwable.class, MessageParts.Throwable);
        assertTrue(thrown instanceof RuntimeException);
        assertTrue(thrown.getMessage().contains(subject));
        finishTest();
      }));
      MessageBuilder
        .createMessage(subject)
        .signalling()
        .defaultErrorHandling()
        .sendNowWith(bus);
    });
  }

  public void testRemoteNoErrorHandling() {
    final String subject = "ErrorThrowingService";
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        fail("Should not have received message on " + DefaultErrorCallback.CLIENT_ERROR_SUBJECT);
      }));

      final Timer finishTimer = new Timer() {
        @Override
        public void run() {
          finishTest();
        }
      };
      subscriptions.add(() -> finishTimer.cancel());
      finishTimer.schedule(5000);

      MessageBuilder
        .createMessage(subject)
        .signalling()
        .noErrorHandling()
        .sendNowWith(bus);
    });
  }

  public void testLocalDefaultErrorHandling() {
    final String subject = LOCAL_ERROR_THROWING_SERVICE;
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        final Throwable thrown = message.get(Throwable.class, MessageParts.Throwable);
        assertTrue(thrown instanceof RuntimeException);
        assertTrue(thrown.getMessage().contains(subject));
        finishTest();
      }));
      MessageBuilder
        .createMessage(subject)
        .signalling()
        .defaultErrorHandling()
        .sendNowWith(bus);
    });
  }

  public void testLocalNoErrorHandling() {
    final String subject = LOCAL_ERROR_THROWING_SERVICE;
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        fail("Should not have received message on " + DefaultErrorCallback.CLIENT_ERROR_SUBJECT);
      }));

      final Timer finishTimer = new Timer() {
        @Override
        public void run() {
          finishTest();
        }
      };
      subscriptions.add(() -> finishTimer.cancel());
      finishTimer.schedule(5000);

      MessageBuilder
        .createMessage(subject)
        .signalling()
        .noErrorHandling()
        .sendNowWith(bus);
    });
  }

  public void testLocalCustomAndDefaultErrorHandling() {
    final String subject = "NonExistentService";
    runAfterInit(() -> {
      final Ref<Boolean> ref = new Ref<>(false);
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        final Throwable thrown = message.get(Throwable.class, MessageParts.Throwable);
        assertTrue(thrown instanceof NoSubscribersToDeliverTo);
        assertTrue(ref.value);
        finishTest();
      }));
      MessageBuilder
        .createMessage(subject)
        .signalling()
        .errorsHandledBy((ErrorCallback<Message>) (m, t) -> {
          ref.value = true;
          return true;
        })
        .sendNowWith(bus);
    });
  }

  public void testLocalCustomOnlyErrorHandling() {
    final String subject = "NonExistentService";
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        fail("Should not have received error on subject " + DefaultErrorCallback.CLIENT_ERROR_SUBJECT);
      }));
      MessageBuilder
        .createMessage(subject)
        .signalling()
        .errorsHandledBy((ErrorCallback<Message>) (m, t) -> {
          final Timer finishTimer = new Timer() {
            @Override
            public void run() {
              finishTest();
            }
          };
          subscriptions.add(() -> {
            finishTimer.cancel();
          });
          finishTimer.schedule(5000);
          return false;
        })
        .sendNowWith(bus);
    });
  }

  public void testRemoteRPCDefaultErrorHandling() {
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        final Throwable thrown = message.get(Throwable.class, MessageParts.Throwable);
        assertNotNull(thrown);
        assertNotNull(thrown.getMessage());
        assertTrue(thrown.getMessage().contains(ErrorThrowingRPCService.class.getSimpleName()));
        finishTest();
      }));
      MessageBuilder
        .createCall((Long time) -> fail("Should not receive response."), ErrorThrowingRPCService.class)
        .currentTime();
    });
  }

  public void testRemoteRPCCustomOnlyErrorHandling() {
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        fail("Should not have received error on subject " + DefaultErrorCallback.CLIENT_ERROR_SUBJECT);
      }));
      MessageBuilder
        .createCall((Long time) -> fail("Should not receive response."),
                    (m, t) -> {
                      final Timer finishTimer = new Timer() {
                        @Override
                        public void run() {
                          finishTest();
                        }
                      };
                      subscriptions.add(() -> {
                        finishTimer.cancel();
                      });
                      finishTimer.schedule(5000);
                      return false;
                    },
                    ErrorThrowingRPCService.class)
        .currentTime();
    });
  }

  public void testRemoteRPCCustomAndDefaultErrorHandling() {
    runAfterInit(() -> {
      subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, message -> {
        final Throwable thrown = message.get(Throwable.class, MessageParts.Throwable);
        assertNotNull(thrown);
        assertNotNull(thrown.getMessage());
        assertTrue(thrown.getMessage().contains(ErrorThrowingRPCService.class.getSimpleName()));
        finishTest();
      }));
      MessageBuilder
        .createCall((Long time) -> fail("Should not receive response."),
                    (m, t) -> {
                      assertNotNull(t);
                      assertNotNull(t.getMessage());
                      assertTrue(t.getMessage().contains(ErrorThrowingRPCService.class.getSimpleName()));
                      return true;
                    },
                    ErrorThrowingRPCService.class)
        .currentTime();
    });
  }

  public void testRemoteRPCUncaughtExceptionHandlerInvokedForErrorInClientCallback() {
    runAfterInit(() -> {
      final Timer failTimer = new Timer() {
        @Override
        public void run() {
          fail("Timed-out without UncaughtExceptionHandler being invoked.");
        }
      };
      final UncaughtExceptionHandler handler = t -> finishTest();
      ((ClientMessageBusImpl) bus).addUncaughtExceptionHandler(handler);

      subscriptions.add(() -> failTimer.cancel());
      subscriptions.add(() -> ((ClientMessageBusImpl) bus).removeUncaughtExceptionHandler(handler));

      MessageBuilder
        .createCall((Long time) -> {
          throw new RuntimeException("Client callback exception.");
        }, ErrorThrowingRPCService.class)
        .currentTime();
      failTimer.schedule(5000);
    });
  }

  public void testRemoteRPCUncaughtExceptionHandlerInvokedWhenErrorHandlerSetForErrorInClientCallback() {
    runAfterInit(() -> {
      final Timer failTimer = new Timer() {
        @Override
        public void run() {
          fail("Timed-out without UncaughtExceptionHandler being invoked.");
        }
      };
      final UncaughtExceptionHandler handler = t -> finishTest();
      ((ClientMessageBusImpl) bus).addUncaughtExceptionHandler(handler);

      subscriptions.add(() -> failTimer.cancel());
      subscriptions.add(() -> ((ClientMessageBusImpl) bus).removeUncaughtExceptionHandler(handler));

      MessageBuilder
        .createCall((Long time) -> {
          throw new RuntimeException("Client callback exception.");
        },
        (m, t) -> {
          fail("Should not invoke error handler for error in client callback.");
          return false;
        },
        SimpleRPCService.class)
        .currentTime();
      failTimer.schedule(5000);
    });
  }
}
