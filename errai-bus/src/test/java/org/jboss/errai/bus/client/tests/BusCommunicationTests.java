/*
 * Copyright (C) 2010 Red Hat, Inc. and/or its affiliates.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.tests.support.GenericServiceB;
import org.jboss.errai.bus.client.tests.support.NonPortableException;
import org.jboss.errai.bus.client.tests.support.Person;
import org.jboss.errai.bus.client.tests.support.RandomProvider;
import org.jboss.errai.bus.client.tests.support.SType;
import org.jboss.errai.bus.client.tests.support.SpecificEntity;
import org.jboss.errai.bus.client.tests.support.SubService;
import org.jboss.errai.bus.client.tests.support.TestException;
import org.jboss.errai.bus.client.tests.support.TestInterceptorRPCService;
import org.jboss.errai.bus.client.tests.support.TestRPCService;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.protocols.MessageParts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Timer;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BusCommunicationTests extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    originalHandler = GWT.getUncaughtExceptionHandler();
    testHandler = new TestUncaughtExceptionHandler(originalHandler);
    GWT.setUncaughtExceptionHandler(testHandler);
    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    GWT.setUncaughtExceptionHandler(originalHandler);
    for (final Subscription sub : subscriptions) {
      sub.remove();
    }
    subscriptions.clear();
    super.gwtTearDown();
  }

  public void testBasicRoundTrip() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        subscriptions.add(bus.subscribe("MyTestService", new MessageCallback() {
          @Override
          public void callback(final Message message) {
            finishTest();
          }
        }));

        MessageBuilder.createMessage()
            .toSubject("ServerEchoService")
            .with(MessageParts.ReplyTo, "MyTestService")
            .done().sendNowWith(bus);
      }
    });
  }

  private int replies = 0;
  private UncaughtExceptionHandler originalHandler;
  private TestUncaughtExceptionHandler testHandler;
  private final Collection<Subscription> subscriptions = new ArrayList<Subscription>();

  public void testBasicRoundTripWithGiantString() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        subscriptions.add(bus.subscribe("GiantStringClient", new MessageCallback() {
          @Override
          public void callback(final Message message) {
            if (++replies == 51)
              finishTest();
          }
        }));

        MessageBuilder.createMessage()
            .toSubject("GiantStringTestService")
            .with(MessageParts.ReplyTo, "GiantStringClient")
            .done().sendNowWith(bus);
      }
    });
  }

  public void testBasicRoundTripWithoutToSubjectCall() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        subscriptions.add(bus.subscribe("MyTestService", new MessageCallback() {
          @Override
          public void callback(final Message message) {
            finishTest();
          }
        }));

        MessageBuilder.createMessage("ServerEchoService")
            .with(MessageParts.ReplyTo, "MyTestService")
            .done().sendNowWith(bus);
      }
    });
  }

  public static class GWTRandomProvider implements RandomProvider {
    private static final char[] CHARS = {
        'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l',
        'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9'
    };

    @Override
    public boolean nextBoolean() {
      return com.google.gwt.user.client.Random.nextBoolean();
    }

    @Override
    public int nextInt(final int upper) {
      return com.google.gwt.user.client.Random.nextInt(upper);
    }

    @Override
    public double nextDouble() {
      return com.google.gwt.user.client.Random.nextDouble();
    }

    @Override
    public char nextChar() {
      return CHARS[com.google.gwt.user.client.Random.nextInt(1000) % CHARS.length];
    }

    @Override
    public String randString() {
      final StringBuilder builder = new StringBuilder();
      final int len = nextInt(25) + 5;
      for (int i = 0; i < len; i++) {
        builder.append(nextChar());
      }
      return builder.toString();
    }
  }

  public void testSerializableCase() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        try {
          final SType sType1 = SType.create(new GWTRandomProvider());

          subscriptions.add(bus.subscribe("ClientReceiver", new MessageCallback() {
            @Override
            public void callback(final Message message) {
              final SType type = message.get(SType.class, "SType");

              try {
                assertNotNull(type);
                assertEquals(sType1, type);
                finishTest();
                return;
              }
              catch (Throwable e) {
                e.printStackTrace();
              }
              fail();
            }
          }));

          MessageBuilder.createMessage()
              .toSubject("TestService1")
              .with("SType", sType1)
              .with(MessageParts.ReplyTo, "ClientReceiver")
              .done().sendNowWith(bus);
        }
        catch (Throwable t) {
          t.printStackTrace(System.out);
        }
      }
    });
  }

  public void testSerializableCase2() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final User user = User.create();

        subscriptions.add(bus.subscribe("ClientReceiver2", new MessageCallback() {
          @Override
          public void callback(final Message message) {
            final User u = message.get(User.class, "User");

            try {
              assertNotNull(u);
              assertTrue(user.toString().equals(u.toString()));

              finishTest();
              return;
            }
            catch (Throwable t) {
              t.printStackTrace();
            }
            fail();
          }
        }));

        MessageBuilder.createMessage()
            .toSubject("TestService2")
            .with("User", user)
            .with(MessageParts.ReplyTo, "ClientReceiver2")
            .done().sendNowWith(bus);
      }
    });
  }

  public void testRpc() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final TestRPCService remote = MessageBuilder.createCall(new RemoteCallback<Boolean>() {
          @Override
          public void callback(final Boolean response) {
            assertTrue(response);
            finishTest();
          }
        }, TestRPCService.class);

        remote.isGreaterThan(10, 5);
      }
    });
  }

  /**
   * Regression test for ERRAI-282 under the CDI implementation of ErraiRPC.
   * Note that there is a similar test in ErraiCDI, which has a strikingly
   * similar, yet independent, implementation of ErraiRPC.
   */
  public void testRpcToInheritedMethod() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final SubService remote = MessageBuilder.createCall(new RemoteCallback<Integer>() {
          @Override
          public void callback(final Integer response) {
            assertNotNull(response);
            assertEquals(1, (int) response);
            finishTest();
          }
        }, SubService.class);

        remote.baseServiceMethod(); // this is a service method inherited from the super interface
      }
    });
  }

  public void testRpcThrowingException() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(
            new RemoteCallback<Object>() {
              @Override
              public void callback(final Object response) {
              }
            },
            new BusErrorCallback() {
              @Override
              public boolean error(final Message message, final Throwable caught) {
                assertNotNull("Message is null.", message);

                String additionalDetails = message.get(String.class, MessageParts.AdditionalDetails);
                assertNotNull("Server-provided stack trace is null.", additionalDetails);
                assertTrue("Additional detail string did not contain a frame for the RPC call. Message contents:\n\n" + message,
                        additionalDetails.contains("TestRPCServiceImpl.exception("));

                assertNotNull("Exception is null.", caught);
                System.out.println("The exception delivered to the error handler (portable case):");
                assertEquals("Received wrong kind of Throwable.", TestException.class, caught.getClass());
                assertStackContains("TestRPCServiceImpl.exception(", caught);
                finishTest();
                return false;
              }
            },
            TestRPCService.class
        ).exception();
      }
    });
  }

  public void testRpcThrowingNonPortableException() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(
            new RemoteCallback<Object>() {
              @Override
              public void callback(final Object response) {
              }
            },
            new BusErrorCallback() {
              @Override
              public boolean error(final Message message, final Throwable caught) {
                assertNotNull("Message is null.", message);

                String additionalDetails = message.get(String.class, MessageParts.AdditionalDetails);
                assertNotNull("Server-provided stack trace is null.", additionalDetails);
                assertTrue("Additional detail string did not contain a frame for the RPC call. Message contents:\n\n" + message,
                        additionalDetails.contains("TestRPCServiceImpl.nonPortableException("));

                assertNotNull("Throwable is null.", caught);
                System.out.println("The exception delivered to the error handler (non-portable case):");
                caught.printStackTrace(System.out);
                try {
                  throw caught;
                }
                catch (Throwable throwable) {
                  assertEquals(NonPortableException.class.getName() + ":" + "message", throwable.getMessage());
                  finishTest();
                }
                return false;
              }
            },
            TestRPCService.class
        ).nonPortableException();
      }
    });
  }
  public void testRpcReturningVoid() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(
            new RemoteCallback<Void>() {
              @Override
              public void callback(final Void response) {
                finishTest();
              }
            },
            TestRPCService.class).returnVoid();
      }
    });
  }

  public void testRpcReturningNull() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(
            new RemoteCallback<Person>() {
              @Override
              public void callback(final Person response) {
                assertNull(response);
                finishTest();

              }
            },
            TestRPCService.class).returnNull();
      }
    });
  }

  public void testRpcToGenericService() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final GenericServiceB remote = MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertNotNull(response);
            assertEquals("SpecificEntity", response);
            finishTest();
          }
        }, GenericServiceB.class);

        remote.create(new SpecificEntity());
      }
    });
  }

  /**
   * Related to issue: https://issues.jboss.org/browse/ERRAI-318
   */
  public void testVarArgsToRPC1() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("foobar", response);
            finishTest();
          }
        }, TestRPCService.class).testVarArgs("foo", "bar");
      }
    });
  }

  public void testVarArgsToRPC2() {
    runAfterInit(new Runnable() {
      @SuppressWarnings("NullArgumentToVariableArgMethod")
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("foo", response);
            finishTest();
          }
        }, TestRPCService.class).testVarArgs("foo", null);
      }
    });
  }

  public void testVarArgsToRPC3() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("foo", response);
            finishTest();
          }
        }, TestRPCService.class).testVarArgs("foo");
      }
    });
  }

  public void testMultipleEndpointsOnRemoteService() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        class TestCount {
          private int countdown;

          TestCount(final int countdown) {
            this.countdown = countdown;
          }

          public boolean done() {
            return --countdown == 0;
          }
        }

        final TestCount testCount = new TestCount(2);

        MessageBuilder.createMessage()
            .toSubject("TestSvc")
            .command("bar")
            .done().repliesTo(new MessageCallback() {

          @Override
          public void callback(final Message message) {
            assertEquals("Bar!", message.get(String.class, "Msg"));
            if (testCount.done()) {
              finishTest();
            }
          }
        }).sendGlobalWith(ErraiBus.get());

        MessageBuilder.createMessage()
            .toSubject("TestSvc")
            .command("foo")
            .done().repliesTo(new MessageCallback() {
          @Override
          public void callback(final Message message) {
            assertEquals("Foo!", message.get(String.class, "Msg"));
            if (testCount.done()) {
              finishTest();
            }
          }
        }).sendGlobalWith(ErraiBus.get());
      }
    });
  }

  public void testCommandMessageThrowingException() {
    testHandler.setExpectedExceptionTypes(RuntimeException.class);
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage()
            .toSubject("TestSvc")
            .command("baz")
            .errorsHandledBy(new BusErrorCallback() {
              @Override
              public boolean error(final Message message, final Throwable throwable) {
                fail("An exception thrown by a MessageCallback should not be delivered to the caller!");
                return false;
              }
            })
            .repliesTo(new MessageCallback() {
              @Override
              public void callback(final Message message) {
                fail("This service throws an Exception and does not reply. " +
                    "The MessageCallback should not have been invoked.");
              }
            })
            .sendGlobalWith(ErraiBus.get());
      }
    });

    new Timer() {
      @Override
      public void run() {
        finishTest();
      }
    }.schedule(7000);
  }

  public void testNonExistingCommandMessage() {
    testHandler.setExpectedExceptionTypes(RuntimeException.class);

    runAfterInit(new Runnable() {
      @Override
      public void run() {
        subscriptions.add(bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
          @Override
          public void callback(final Message message) {
            final String errorMessage = message.get(String.class, MessageParts.ErrorMessage);
            final String additionalDetails = message.get(String.class, MessageParts.AdditionalDetails);
            assertTrue("Throwable should contain non-existing service name."
                    + "\n\tObserved errorMessage: " + errorMessage
                    + "\n\tObserved additionalDetails: " + additionalDetails,
                (errorMessage + additionalDetails).contains("non-existing"));
            finishTest();
          }
        }));

        MessageBuilder.createMessage()
            .toSubject("TestSvc")
            .command("non-existing")
            .done()
            .repliesTo(new MessageCallback() {
              @Override
              public void callback(final Message message) {
                fail("Callback should not have been invoked!");
              }
            })
            .sendGlobalWith(ErraiBus.get());
      }
    });
  }

  public void testInterceptedRpcWithEndpointBypassing() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("Request was not intercepted", "intercepted", response);
            finishTest();
          }
        }, TestRPCService.class)
            .interceptedRpcWithEndpointBypassing();
      }
    });
  }

  public void testInterceptedRpcWithResultManipulation() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("Request was not intercepted", "result_intercepted", response);
            finishTest();
          }
        }, TestRPCService.class).interceptedRpcWithResultManipulation();
      }
    });
  }

  public void testInterceptedRpcWithParameterManipulation() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("Request was not intercepted", "interceptor_value", response);
            finishTest();
          }
        }, TestRPCService.class).interceptedRpcWithParameterManipulation("value");
      }
    });
  }

  public void testInterceptedRpc1() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("Request was not intercepted", "intercepted", response);
            finishTest();
          }
        }, TestInterceptorRPCService.class)
            .interceptedRpc1();
      }
    });
  }

  public void testInterceptedRpc2() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("Request was not intercepted", "intercepted", response);
            finishTest();
          }
        }, TestInterceptorRPCService.class)
            .interceptedRpc2();
      }
    });
  }

  public void testInterceptedRpcWithChainedInterceptors() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(final String response) {
            assertEquals("Request was not intercepted", "ABCD", response);
            finishTest();
          }
        }, TestRPCService.class).interceptedRpcWithChainedInterceptors("");
      }
    });
  }

  public void testPlainMessagingWithRpcEndpoint() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        subscriptions.add(bus.subscribe("PlainMessageResponse", new MessageCallback() {
          @Override
          public void callback(final Message message) {
            finishTest();
          }
        }));

        MessageBuilder.createMessage()
            .toSubject("TestRPCServiceImpl")
            .with(MessageParts.ReplyTo, "PlainMessageResponse")
            .done().sendNowWith(bus);
      }
    });
  }

  public void testBusUnsubscribe() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final String subjectToSubscribe = "testBusUnsubscribeTestSubjectThatWillBeRemovedAndThrowAnException";

        final Subscription subs = bus.subscribe(subjectToSubscribe, new MessageCallback() {
          @Override
          public void callback(final Message message) {
          }
        });

        subs.remove();

        try {
          MessageBuilder.createMessage()
              .toSubject(subjectToSubscribe).done().sendNowWith(bus);
        }
        catch (NoSubscribersToDeliverTo e) {
          finishTest();
          return;
        }

        fail("should have thrown exception because service should have been de-registered");
      }
    });
  }

  static void assertStackContains(String string, Throwable t) {
    while (t != null) {
      for (StackTraceElement ste : t.getStackTrace()) {
        if (ste.toString().contains(string)) {
          return;
        }
      }
      t = t.getCause();
    }
    fail("Stack trace does not contain the string: " + string);
  }

  @SuppressWarnings("rawtypes")
  static class TestUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private final UncaughtExceptionHandler originalHandler;
    final Set<Class> ignorable;

    public TestUncaughtExceptionHandler(final UncaughtExceptionHandler originalHandler) {
      this.originalHandler = originalHandler;
      ignorable = new HashSet<Class>();
    }

    public void setExpectedExceptionTypes(final Class... expectedExceptionTypes) {
      setExpectedExceptionTypes(Arrays.asList(expectedExceptionTypes));
    }

    public void setExpectedExceptionTypes(final Collection<Class> expectedExceptionTypes) {
      ignorable.clear();
      ignorable.addAll(expectedExceptionTypes);
    }

    @Override
    public void onUncaughtException(final Throwable e) {
      if (e != null) {
        if (!ignorable.contains(e.getClass())) {
          originalHandler.onUncaughtException(e);
        }
      }
    }

  }
}
