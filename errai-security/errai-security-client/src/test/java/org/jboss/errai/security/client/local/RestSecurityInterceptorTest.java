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

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Default;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.NoOpCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsModule;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.res.Counter;
import org.jboss.errai.security.client.local.res.CountingRemoteCallback;
import org.jboss.errai.security.client.local.res.RestErrorCountingCallback;
import org.jboss.errai.security.client.local.res.RestSecurityTestModule;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.client.shared.SecureRestService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.Test;

import com.google.gwt.user.client.Timer;

/**
 * Most of the logic within the client-side interceptors is tested in
 * {@link BusSecurityInterceptorTest}. This test class ensures that the same
 * interceptors are properly generated for jaxrs endpoints (as well as the
 * server-side interceptors).
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class RestSecurityInterceptorTest extends AbstractSecurityInterceptorTest {

  public static final String BASE_URL = "/";

  private ActiveUserCache activeUserCache;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new JaxrsModule().onModuleLoad();
    RestClient.setApplicationRoot(BASE_URL);
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new NoOpCallback<Void>(), AuthenticationService.class).logout();
      }
    });
    activeUserCache = IOC.getBeanManager().lookupBean(ActiveUserCache.class, new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Default.class;
      }
    }).getInstance();
  }

  @Test
  public void testAnybodyServiceNotblocked() throws Exception {
    final Counter callbackCounter = new Counter();
    helper(new Runnable() {
      @Override
      public void run() {
        restCallHelper(new CountingRemoteCallback(callbackCounter), null).anybody();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, callbackCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testAdminBlockedWhenNotLoggedIn() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingRemoteCallback(callbackCounter);
    final Counter errorCounter = new Counter();
    final RestErrorCallback errorCallback = new RestErrorCountingCallback(errorCounter, UnauthenticatedException.class);

    helper(new Runnable() {
      @Override
      public void run() {
        restCallHelper(callback, errorCallback).admin();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, callbackCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testUserBlockedWhenNotLoggedIn() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingRemoteCallback(callbackCounter);
    final Counter errorCounter = new Counter();
    final RestErrorCallback errorCallback = new RestErrorCountingCallback(errorCounter, UnauthenticatedException.class);

    helper(new Runnable() {
      @Override
      public void run() {
        restCallHelper(callback, errorCallback).user();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, callbackCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testUserAllowedWhenLoggedIn() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingRemoteCallback(callbackCounter);

    helper(new Runnable() {
      @Override
      public void run() {
        // backup timer in case neither call back happens.
        final Timer backup = new Timer() {

          @Override
          public void run() {
            fail("Precondition failed: Timed out while waiting to log in.");
          }

        };
        backup.schedule((int) TIME_LIMIT - 2000);

        MessageBuilder.createCall(new RemoteCallback<User>() {

          @Override
          public void callback(User response) {
            backup.cancel();
            restCallHelper(callback, null).user();
            testUntil(TIME_LIMIT, new Runnable() {
              @Override
              public void run() {
                assertEquals(1, callbackCounter.getCount());
              }
            });
          }
        }, new BusErrorCallback() {

          @Override
          public boolean error(Message message, Throwable throwable) {
            backup.cancel();
            fail("Precondition failed: Couldn't log in.");
            return false;
          }
        }, AuthenticationService.class).login("user", "password");
      }
    });
  }

  @Test
  public void testAdminBlockedWhenNotLoggedInAsUser() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingRemoteCallback(callbackCounter);
    final Counter errorCounter = new Counter();
    final RestErrorCallback errorCallback = new RestErrorCountingCallback(errorCounter, UnauthorizedException.class);
    final User user = new UserImpl("user");

    helper(new Runnable() {
      @Override
      public void run() {
        activeUserCache.setUser(user);
        restCallHelper(callback, errorCallback).admin();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, callbackCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testServerSideAuthorizationInterceptorNotAuthorized() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingRemoteCallback(callbackCounter);
    final Counter errorCounter = new Counter();
    final RestErrorCallback errorCallback = new RestErrorCountingCallback(errorCounter, UnauthorizedException.class);

    helper(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            activeUserCache.invalidateCache();
            restCallHelper(callback, errorCallback).admin();
          }
        }, new BusErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
            fail("Precondition failed: could not log in.");
            return false;
          }
        }, AuthenticationService.class).login("user", "123");

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, callbackCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  @Test
  public void testServerSideAuthenticationInterceptorNotLoggedIn() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingRemoteCallback(callbackCounter);
    final Counter errorCounter = new Counter();
    final RestErrorCallback errorCallback = new RestErrorCountingCallback(errorCounter, UnauthenticatedException.class);

    helper(new Runnable() {
      @Override
      public void run() {
        activeUserCache.invalidateCache();
        restCallHelper(callback, errorCallback).admin();

        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(0, callbackCounter.getCount());
            assertEquals(1, errorCounter.getCount());
          }
        });
      }
    });
  }

  private void helper(final Runnable test) {
    delayTestFinish((int) TIME_LIMIT);
    afterLogout(test);
  }

  private static SecureRestService restCallHelper(final RemoteCallback<Void> callback,
          final RestErrorCallback errorCallback) {
    final Caller<SecureRestService> caller = Factory
            .maybeUnwrapProxy(IOC.getBeanManager().lookupBean(RestSecurityTestModule.class).getInstance()).restCaller;

    if (errorCallback != null) {
      return caller.call(callback, errorCallback);
    }
    else {
      return caller.call(callback);
    }
  }

}
