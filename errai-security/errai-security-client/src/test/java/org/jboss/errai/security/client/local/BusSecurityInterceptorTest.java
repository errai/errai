/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.client.local;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.*;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Default;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.VoidCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.res.Counter;
import org.jboss.errai.security.client.local.res.CountingMessageCallback;
import org.jboss.errai.security.client.local.res.ErrorCountingCallback;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.client.shared.AdminService;
import org.jboss.errai.security.client.shared.AuthenticatedService;
import org.jboss.errai.security.client.shared.DiverseService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.Test;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class BusSecurityInterceptorTest extends AbstractSecurityInterceptorTest {

  private final Annotation defaultAnno = new Annotation() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Default.class;
    }
  };

  private ActiveUserCache provider;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    provider = IOC.getBeanManager().lookupBean(ActiveUserCache.class, defaultAnno).getInstance();
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new VoidCallback(), AuthenticationService.class).logout();
      }
    });
  }

  @Test
  public void testAuthInterceptorNotLoggedInHomogenous() throws Exception {
    asyncTest();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class),
        AuthenticatedService.class)
        .userStuff();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testAuthInterceptorRedirectsWithNoErrorHandler() throws
  Exception {
    asyncTest();
    runNavTest(new Runnable() {
      @Override
      public void run() {
        final TestLoginPage page =
                IOC.getBeanManager().lookupBean(TestLoginPage.class).getInstance();
        assertEquals(0, page.getPageLoadCounter());

        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, AuthenticatedService.class)
        .userStuff();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, page.getPageLoadCounter());
          }
        });
      }
    });
  }

  @Test
  public void testAuthInterceptorNotLoggedInHeterogenous() throws Exception {
    asyncTest();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());

        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class),
        DiverseService.class)
        .needsAuthentication();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testAuthInterceptorLoggedInHeterogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class),
            DiverseService.class)
            .needsAuthentication();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(1, counter.getCount());
                assertEquals(0, errorCounter.getCount());
              }
            });
          }
        }, AuthenticationService.class).login("john", "123");
      }
    });
  }

  @Test
  public void testAuthInterceptorLoggedInHomogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class),
            AuthenticatedService.class)
            .userStuff();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(1, counter.getCount());
                assertEquals(0, errorCounter.getCount());
              }
            });
          }
        }, AuthenticationService.class).login("john", "123");
      }
    });
  }

  @Test
  public void testRoleInterceptorNotLoggedInHomogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());

        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class),
        AdminService.class).adminStuff();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void
  testRoleInterceptorRedirectsToLoginWhenNotLoggedInAndNoErrorHandler()
          throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    runNavTest(new Runnable() {
      @Override
      public void run() {
        final TestLoginPage page =
                IOC.getBeanManager().lookupBean(TestLoginPage.class).getInstance();
        assertEquals(0, page.getPageLoadCounter());

        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());

        assertEquals(0, counter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, AdminService.class).adminStuff();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, page.getPageLoadCounter());
          }
        });
      }
    });
  }

  @Test
  public void testRoleInterceptorNotLoggedInHeterogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());

        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class),
        DiverseService.class).adminOnly();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testRoleInterceptorLoggedInUnprivelegedHeterogenous() throws
  Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            assertEquals(0, errorCounter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class),
            DiverseService.class).adminOnly();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(0, counter.getCount());
                assertEquals(1, errorCounter.getCount());
              }
            });
          }
        }, AuthenticationService.class).login("john", "123");
      }
    });
  }

  @Test
  public void testRoleInterceptorLoggedInUnprivelegedRedirectsToSecurityErrorWithNoErrorCallback() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    runNavTest(new Runnable() {
      @Override
      public void run() {
        final TestSecurityErrorPage page = IOC.getBeanManager().lookupBean(TestSecurityErrorPage.class)
                .getInstance();
        assertEquals(0, page.getPageLoadCounter());

        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, DiverseService.class).adminOnly();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(0, counter.getCount());
                assertEquals(1, page.getPageLoadCounter());
              }
            });
          }
        }, AuthenticationService.class).login("john", "123");
      }
    });
  }

  @Test
  public void testRoleInterceptorLoggedInUnprivelegedHomogenous() throws
  Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            assertEquals(0, errorCounter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class),
            AdminService.class).adminStuff();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(0, counter.getCount());
                assertEquals(1, errorCounter.getCount());
              }
            });
          }
        }, AuthenticationService.class).login("john", "123");
      }
    });
  }

  @Test
  public void testRoleInterceptorLoggedInPrivelegedHomogenous() throws
  Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            assertEquals(0, errorCounter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class),
            AdminService.class).adminStuff();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(0, errorCounter.getCount());
                assertEquals(1, counter.getCount());
              }
            });
          }
        }, AuthenticationService.class).login("admin", "123");
      }
    });
  }

  @Test
  public void testRoleInterceptorLoggedInPrivelegedHeterogenous() throws
  Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(0, counter.getCount());
            assertEquals(0, errorCounter.getCount());
            createCall(new RemoteCallback<Void>() {
              @Override
              public void callback(Void response) {
                counter.increment();
              }
            }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class),
            DiverseService.class).adminOnly();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(1, counter.getCount());
                assertEquals(0, errorCounter.getCount());
              }
            });
          }
        }, AuthenticationService.class).login("admin", "123");
      }
    });
  }

  @Test
  public void testSecureCallbackNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("SecureMessageCallback")
        .signalling()
        .errorsHandledBy(new ErrorCountingCallback(errorCounter, UnauthenticatedException.class))
        .repliesTo(new CountingMessageCallback(counter))
        .sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testSecureClassNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("methodInSecureClass")
        .signalling()
        .errorsHandledBy(new ErrorCountingCallback(errorCounter, UnauthenticatedException.class))
        .repliesTo(new CountingMessageCallback(counter))
        .sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testSecureMethodNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("secureMethod")
        .signalling()
        .errorsHandledBy(new ErrorCountingCallback(errorCounter, UnauthenticatedException.class))
        .repliesTo(new CountingMessageCallback(counter))
        .sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, counter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testInsecureMethodInClassWithSecureMethodNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("insecureMethod")
        .signalling()
        .errorsHandledBy(new ErrorCountingCallback(errorCounter, UnauthenticatedException.class))
        .repliesTo(new CountingMessageCallback(counter))
        .sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, counter.getCount());
            assertEquals(0, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testCommandMethodInSecureClassNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("commandMethodInSecureClass")
        .command("command")
        .errorsHandledBy(new ErrorCountingCallback(errorCounter, UnauthenticatedException.class))
        .repliesTo(new CountingMessageCallback(counter))
        .sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, counter.getCount());
            assertEquals(0, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testSecureCommandMethodNotLoggedIn() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createMessage("secureCommandMethod")
        .command("command")
        .errorsHandledBy(new ErrorCountingCallback(errorCounter, UnauthenticatedException.class))
        .repliesTo(new CountingMessageCallback(counter))
        .sendNowWith(ErraiBus.get());

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, counter.getCount());
            assertEquals(0, errorCounter.getCount());
          }
        });
      }
    });
  }
}
