/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.client.tests;

import com.google.common.eventbus.Subscribe;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.tests.support.GenericServiceB;
import org.jboss.errai.bus.client.tests.support.Person;
import org.jboss.errai.bus.client.tests.support.RandomProvider;
import org.jboss.errai.bus.client.tests.support.SType;
import org.jboss.errai.bus.client.tests.support.SpecificEntity;
import org.jboss.errai.bus.client.tests.support.SubService;
import org.jboss.errai.bus.client.tests.support.TestException;
import org.jboss.errai.bus.client.tests.support.TestRPCService;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.common.client.protocols.MessageParts;

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

  public void testBasicRoundTrip() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        bus.subscribe("MyTestService", new MessageCallback() {
          @Override
          public void callback(Message message) {
            finishTest();
          }
        });

        MessageBuilder.createMessage()
                .toSubject("ServerEchoService")
                .with(MessageParts.ReplyTo, "MyTestService")
                .done().sendNowWith(bus);
      }
    });
  }

  private int replies = 0;

  public void testBasicRoundTripWithGiantString() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        bus.subscribe("GiantStringClient", new MessageCallback() {
          @Override
          public void callback(Message message) {
            if (++replies == 51)
              finishTest();
          }
        });

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
        bus.subscribe("MyTestService", new MessageCallback() {
          @Override
          public void callback(Message message) {
            finishTest();
          }
        });

        MessageBuilder.createMessage("ServerEchoService")
                .with(MessageParts.ReplyTo, "MyTestService")
                .done().sendNowWith(bus);
      }
    });
  }

  public static class GWTRandomProvider implements RandomProvider {
    private static char[] CHARS = {
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
    public int nextInt(int upper) {
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
      int len = nextInt(25) + 5;
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

          bus.subscribe("ClientReceiver", new MessageCallback() {
            @Override
            public void callback(Message message) {
              SType type = message.get(SType.class, "SType");

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
          });

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

        bus.subscribe("ClientReceiver2", new MessageCallback() {
          @Override
          public void callback(Message message) {
            User u = message.get(User.class, "User");

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
        });

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
        TestRPCService remote = MessageBuilder.createCall(new RemoteCallback<Boolean>() {
          @Override
          public void callback(Boolean response) {
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
        SubService remote = MessageBuilder.createCall(new RemoteCallback<Integer>() {
          @Override
          public void callback(Integer response) {
            assertNotNull(response);
            assertEquals(1, (int) response);
            finishTest();
          }
        }, SubService.class);

        remote.baseServiceMethod(); // this is a service method inherited from the superinterface
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
              public void callback(Object response) {
              }
            },
            new ErrorCallback() {
              @Override
              public boolean error(final Message message, final Throwable caught) {
                assertNotNull("Message is null.", message);
                assertNotNull("Throwable is null.", caught);

                try {
                  throw caught;
                }
                catch (TestException e) {
                  finishTest();
                }
                catch (Throwable throwable) {
                  fail("Received wrong Throwable.");
                }
                return false;
              }
            },
            TestRPCService.class
        ).exception();
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
              public void callback(Void response) {
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
                  public void callback(Person response) {
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
          public void callback(String response) {
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
          public void callback(String response) {
            assertEquals("foobar", response);
            finishTest();
          }
        }, TestRPCService.class).testVarArgs("foo", "bar");
      }
    });
  }

  public void testVarArgsToRPC2() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
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
          public void callback(String response) {
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

          TestCount(int countdown) {
            this.countdown = countdown;
          }

          public boolean done() {
            return --countdown == 0;
          }
        }

        final TestCount testCount = new TestCount(2);

        MessageBuilder.createMessage()
                .toSubject("TestSvc")
                .command("foo")
                .done().repliesTo(new MessageCallback() {
          @Override
          public void callback(Message message) {
            assertEquals("Foo!", message.get(String.class, "Msg"));
            if (testCount.done()) {
              finishTest();
            }
          }
        }).sendGlobalWith(ErraiBus.get());

        MessageBuilder.createMessage()
                .toSubject("TestSvc")
                .command("bar")
                .done().repliesTo(new MessageCallback() {

          @Override
          public void callback(Message message) {
            assertEquals("Bar!", message.get(String.class, "Msg"));
            if (testCount.done()) {
              finishTest();
            }
          }
        }).sendGlobalWith(ErraiBus.get());
      }
    });
  }

  public void testCommandMessageThrowingException() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage()
          .toSubject("TestSvc")
          .command("baz")
          .errorsHandledBy(new ErrorCallback() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                fail("An exception thrown by a MessageCallback should not be delivered to the caller!");
                return false;
              }
            })
          .repliesTo(new MessageCallback() {
              @Override
              public void callback(Message message) {
                fail("This service throws an Exception and does not reply. " +
                		"The MessageCallback should not have been invoked.");
              }
            })
        .sendGlobalWith(ErraiBus.get());
      }
    }, 20000);
    
    new Timer() {
      @Override
      public void run() {
        finishTest();
        
      }
    }.schedule(15000);
  }
  
  public void testNonExistingCommandMessage() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
          @Override
          public void callback(Message message) {
            assertTrue("throwable should contain non-existing service name", 
                message.get(String.class, MessageParts.ErrorMessage).contains("non-existing"));
            finishTest();
          }
        });
        
        MessageBuilder.createMessage()
          .toSubject("TestSvc")
          .command("non-existing")
          .done()
          .repliesTo(new MessageCallback() {
              @Override
              public void callback(Message message) {
                fail("Callback should not have been invoked!");
              }
            })
        .sendGlobalWith(ErraiBus.get());
      }
    });
  }
  
  
  public void testMultipleEndpointsOnRemoteServiceWithAuthentication() {
    class TestCount {
      private int countdown;

      TestCount(int countdown) {
        this.countdown = countdown;
      }

      public boolean done() {
        return --countdown == 0;
      }
    }

    final TestCount testCount = new TestCount(2);

    class CommunicationTasks {
      public void tryCommunication() {
        MessageBuilder.createMessage()
                .toSubject("TestSvcAuth")
                .command("foo")
                .done().repliesTo(new MessageCallback() {
          @Override
          public void callback(Message message) {
            assertEquals("Foo!", message.get(String.class, "Msg"));
            if (testCount.done()) {
              finishTest();
            }
          }
        }).sendGlobalWith(ErraiBus.get());

        MessageBuilder.createMessage()
                .toSubject("TestSvcAuth")
                .command("bar")
                .done()
                .repliesTo(new MessageCallback() {
          @Override
          public void callback(Message message) {
            assertEquals("Bar!", message.get(String.class, "Msg"));
            if (testCount.done()) {
              finishTest();
            }
          }
        })
        .sendGlobalWith(ErraiBus.get());
      }
    }

    runAfterInit(new Runnable() {
      @Override
      public void run() {

        // create a login client to handle the authentication handshake
        ErraiBus.get().subscribe("LoginClient", new MessageCallback() {
          @Override
          public void callback(Message message) {
            if (message.getCommandType().equals("FailedAuth")) {
              fail("failed to authenticate with server");
            }
            else if (message.getCommandType().equals("SuccessfulAuth")) {
              new CommunicationTasks().tryCommunication();
            }
            else {
              MessageBuilder.createConversation(message)
                      .subjectProvided()
                      .command(SecurityCommands.AuthRequest)
                      .with(MessageParts.ReplyTo, "LoginClient")
                      .with(SecurityParts.Name, "test")
                      .with(SecurityParts.Password, "test123")
                      .done().reply();
            }
          }
        });

        new CommunicationTasks().tryCommunication();
      }
    });
  }

  public void testPlainMessagingWithRpcEndpoint() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        bus.subscribe("PlainMessageResponse", new MessageCallback() {
          @Override
          public void callback(Message message) {
            finishTest();
          }
        });

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
        final String subjectToSubcribe = "testBusUnsubscribeTestSubjectThatWillBeRemovedAndThrowAnException";

        final Subscription subs = bus.subscribe(subjectToSubcribe, new MessageCallback() {
          @Override
          public void callback(Message message) {
          }
        });

        subs.remove();

        try {
          MessageBuilder.createMessage()
                  .toSubject(subjectToSubcribe).done().sendNowWith(bus);
        }
        catch (NoSubscribersToDeliverTo e) {
          finishTest();
          return;
        }

        fail("should have thrown exception because service should have been de-registered");
      }
    });
  }
}