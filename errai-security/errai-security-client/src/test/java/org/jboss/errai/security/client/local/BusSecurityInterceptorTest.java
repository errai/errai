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

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Default;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.res.Counter;
import org.jboss.errai.security.client.local.res.CountingRemoteCallback;
import org.jboss.errai.security.client.local.res.ErrorCountingCallback;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.client.shared.AdminService;
import org.jboss.errai.security.client.shared.AdminTypeUserMethodService;
import org.jboss.errai.security.client.shared.AuthenticatedService;
import org.jboss.errai.security.client.shared.DiverseService;
import org.jboss.errai.security.client.shared.SecureRoleProvidedService;
import org.jboss.errai.security.client.shared.UserMethodSharedImplService;
import org.jboss.errai.security.client.shared.UserTypeAdminMethodService;
import org.jboss.errai.security.client.shared.UserTypeSharedImplService;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.Navigation;

/**
 * <p>
 * These test cases are run in two modes:
 * <ul>
 * <li>ClientBusSecurityInterceptorTests runs these tests with a valid user cache, causing the
 * client-side security interceptors to be triggered.
 * <li>ServerBusSecurityInterceptorTests runs these tests with an invalid user cache, causing RPCs
 * to bypass the client-side security checks.
 *
 * <p>
 * Note that the client side tests use the ClientInteceptorTestAssistant so that calls not blocked
 * by the client interceptor do not go to the server (making the tests compeletly independent).
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class BusSecurityInterceptorTest extends AbstractSecurityInterceptorTest {

  private final Annotation defaultAnno = new Annotation() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Default.class;
    }
  };

  protected ActiveUserCache provider;

  @Override
  protected void gwtSetUp() throws Exception {
    TIME_LIMIT = 10000;
    super.gwtSetUp();
    provider = IOC.getBeanManager().lookupBean(ActiveUserCache.class, defaultAnno).getInstance();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    IOC.getBeanManager().lookupBean(Navigation.class).getInstance().cleanUp();
    super.gwtTearDown();
  }

  protected abstract void postLogout();

  protected abstract void postLogin(User user);

  @Override
  protected void afterLogout(final Runnable test) {
    super.afterLogout(new Runnable() {
      @Override
      public void run() {
        postLogout();
        test.run();
      }
    });
  }

  protected void afterLogin(final String username, final String password, final RemoteCallback<User> remoteCallback) {
    afterLogout(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(final User user) {
            postLogin(user);
            remoteCallback.callback(user);
          }
        }, AuthenticationService.class).login(username, password);
      }
    });
  }

  public void testAuthInterceptorNotLoggedInHomogenous() throws Exception {
    asyncTest();
    final Counter errorCounter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        assertEquals("Test should run after logout!", User.ANONYMOUS, provider.getUser());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class), AuthenticatedService.class)
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

  public void testAuthInterceptorNotLoggedInHeterogenous() throws Exception {
    asyncTest();
    final Counter errorCounter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        assertEquals("Test should run after logout!", User.ANONYMOUS, provider.getUser());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class), DiverseService.class)
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

  public void testAuthInterceptorLoggedInHeterogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        assertEquals(0, counter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class), DiverseService.class)
                .needsAuthentication();
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

  public void testAuthInterceptorLoggedInHomogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        assertEquals(0, counter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class), AuthenticatedService.class)
                .userStuff();
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

  public void testRoleInterceptorNotLoggedInHomogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());

        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class), AdminService.class).adminStuff();
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

  public void testRoleInterceptorNotLoggedInHeterogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogout(new Runnable() {
      @Override
      public void run() {
        assertEquals("Test should run after logout!", User.ANONYMOUS, provider.getUser());
        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthenticatedException.class), DiverseService.class).adminOnly();
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

  public void testRoleInterceptorLoggedInUnprivelegedHeterogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class), DiverseService.class).adminOnly();
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

  public void testRoleInterceptorLoggedInUnprivelegedHomogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class), AdminService.class).adminStuff();
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

  public void testRoleInterceptorLoggedInPrivelegedHomogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("admin", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User user) {
        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class), AdminService.class).adminStuff();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, errorCounter.getCount());
            assertEquals(1, counter.getCount());
          }
        });
      }
    });
  }

  public void testRoleInterceptorLoggedInPrivelegedHeterogenous() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("admin", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        assertEquals(0, counter.getCount());
        assertEquals(0, errorCounter.getCount());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            counter.increment();
          }
        }, new ErrorCountingCallback(errorCounter, UnauthorizedException.class), DiverseService.class).adminOnly();
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

  public void testRolesFromProviderAreRequiredWithInterceptor() throws Exception {
    asyncTest();
    final Counter successCounter = new Counter();
    final Counter errorCounter = new Counter();
    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        MessageBuilder.createCall(new CountingRemoteCallback(successCounter),
                new ErrorCountingCallback(errorCounter, UnauthorizedException.class), SecureRoleProvidedService.class)
                .secureService();

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, successCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  public void testRolesFromMethodRequiredWhenRolesOnTypeAndMethod() throws Exception {
    asyncTest();

    final Counter successCounter = new Counter();
    final Counter errorCounter = new Counter();

    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        // Preconditions
        assertEquals(1, response.getRoles().size());
        assertEquals(new RoleImpl("user"), response.getRoles().iterator().next());

        MessageBuilder.createCall(new CountingRemoteCallback(successCounter),
                new ErrorCountingCallback(errorCounter, UnauthorizedException.class), UserTypeAdminMethodService.class)
                .someService();

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, successCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  public void testRolesFromTypeRequiredWhenRolesOnTypeAndMethod() throws Exception {
    asyncTest();

    final Counter successCounter = new Counter();
    final Counter errorCounter = new Counter();

    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        // Preconditions
        assertEquals(1, response.getRoles().size());
        assertEquals(new RoleImpl("user"), response.getRoles().iterator().next());

        MessageBuilder.createCall(new CountingRemoteCallback(successCounter),
                new ErrorCountingCallback(errorCounter, UnauthorizedException.class), AdminTypeUserMethodService.class)
                .someService();

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, successCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  public void testOnlyRolesFromRequestedInterfaceMethodRequired() throws Exception {
    asyncTest();

    final Counter successCounter = new Counter();
    final Counter errorCounter = new Counter();

    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        // Preconditions
        assertEquals(1, response.getRoles().size());
        assertEquals(new RoleImpl("user"), response.getRoles().iterator().next());

        MessageBuilder
                .createCall(new CountingRemoteCallback(successCounter),
                        new ErrorCountingCallback(errorCounter, UnauthorizedException.class),
                        UserMethodSharedImplService.class).someUserService();

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, successCounter.getCount());
            assertEquals(0, errorCounter.getCount());
          }
        });
      }
    });
  }

  public void testOnlyRolesFromRequestedInterfaceTypeRequired() throws Exception {
    asyncTest();

    final Counter successCounter = new Counter();
    final Counter errorCounter = new Counter();

    afterLogin("john", "123", new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        // Preconditions
        assertEquals(1, response.getRoles().size());
        assertEquals(new RoleImpl("user"), response.getRoles().iterator().next());

        MessageBuilder
                .createCall(new CountingRemoteCallback(successCounter),
                        new ErrorCountingCallback(errorCounter, UnauthorizedException.class),
                        UserTypeSharedImplService.class).someUserService();

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, successCounter.getCount());
            assertEquals(0, errorCounter.getCount());
          }
        });
      }
    });
  }
}
