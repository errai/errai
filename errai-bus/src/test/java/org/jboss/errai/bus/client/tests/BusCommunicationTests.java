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

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.tests.support.Person;
import org.jboss.errai.bus.client.tests.support.RandomProvider;
import org.jboss.errai.bus.client.tests.support.SType;
import org.jboss.errai.bus.client.tests.support.TestException;
import org.jboss.errai.bus.client.tests.support.TestRPCService;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.common.client.protocols.MessageParts;

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
      public void run() {
        bus.subscribe("MyTestService", new MessageCallback() {
          public void callback(Message message) {
            System.out.println("GOT ECHO");
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
      public void run() {
        bus.subscribe("GiantStringClient", new MessageCallback() {
          public void callback(Message message) {
            System.out.println(message.get(String.class, "string"));
            System.out.println(++replies);
            if (replies == 51)
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
      public void run() {
        bus.subscribe("MyTestService", new MessageCallback() {
          public void callback(Message message) {
            System.out.println("GOT ECHO");
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
    private static char[] CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    public boolean nextBoolean() {
      return com.google.gwt.user.client.Random.nextBoolean();
    }

    public int nextInt(int upper) {
      return com.google.gwt.user.client.Random.nextInt(upper);
    }

    public double nextDouble() {
      return com.google.gwt.user.client.Random.nextDouble();
    }

    public char nextChar() {
      return CHARS[com.google.gwt.user.client.Random.nextInt(1000) % CHARS.length];
    }

    public String randString() {
      StringBuilder builder = new StringBuilder();
      int len = nextInt(25) + 5;
      for (int i = 0; i < len; i++) {
        builder.append(nextChar());
      }
      return builder.toString();
    }
  }


  public void testSerializableCase() {
    runAfterInit(new Runnable() {
      public void run() {
        try {
          final SType sType1 = SType.create(new GWTRandomProvider());

          System.out.println("ORIGINAL: " + sType1.toString());

          bus.subscribe("ClientReceiver", new MessageCallback() {
            public void callback(Message message) {
              SType type = message.get(SType.class, "SType");

              try {
                assertNotNull(type);
                System.out.println("CLIENT: " + type.toString());
                assertEquals(sType1, type);

                //   assertTrue(sType1.equals(type));

                System.out.println("**TEST PASSED**");
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
      public void run() {
        final User user = User.create();

        bus.subscribe("ClientReceiver2", new MessageCallback() {
          public void callback(Message message) {
            User u = message.get(User.class, "User");

            try {
              assertNotNull(u);

              System.out.println("BEFORE: " + user.toString());
              System.out.println("AFTER : " + u.toString());
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

  public void testRPC() {
    runAfterInit(new Runnable() {
      public void run() {
        TestRPCService remote = MessageBuilder.createCall(new RemoteCallback<Boolean>() {
          public void callback(Boolean response) {
            assertTrue(response);
            finishTest();
          }
        }, TestRPCService.class);

        remote.isGreaterThan(10, 5);
      }
    });
  }

  public void testRPCThrowingException() {
    runAfterInit(new Runnable() {
      public void run() {
        MessageBuilder.createCall(
                new RemoteCallback<Object>() {
                  public void callback(Object response) {
                  }
                },
                new ErrorCallback() {
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

  public void testRPCReturningVoid() {
    runAfterInit(new Runnable() {
      public void run() {
        MessageBuilder.createCall(
                new RemoteCallback<Void>() {
                  public void callback(Void response) {
                    finishTest();
                  }
                },
                TestRPCService.class).returnVoid();
      }
    });
  }

  public void testRPCReturningNull() {
    runAfterInit(new Runnable() {
      public void run() {
        MessageBuilder.createCall(
                new RemoteCallback<Person>() {
                  public void callback(Person response) {
                    assertNull(response);
                    finishTest();
                  }
                },
                TestRPCService.class).returnNull();
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
              System.out.println("Yay!");
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
}