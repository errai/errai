package org.jboss.errai.security.client.local;

import java.util.ArrayList;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsModule;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.client.local.res.Counter;
import org.jboss.errai.security.client.local.res.CountingCallback;
import org.jboss.errai.security.client.local.res.RestErrorCountingCallback;
import org.jboss.errai.security.client.local.res.RestSecurityTestModule;
import org.jboss.errai.security.client.shared.SecureRestService;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.junit.Test;

/**
 * Most of the logic within the client-side interceptors is tested in
 * {@link SecurityInterceptorTest}. This test class ensures that the same
 * interceptors are properly generated for jaxrs endpoints.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class RestSecurityInterceptorTest extends AbstractSecurityInterceptorTest {

  public static final String BASE_URL = "/";

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new JaxrsModule().onModuleLoad();
    RestClient.setApplicationRoot(BASE_URL);
    TIME_LIMIT = 60000;
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        IOC.getBeanManager().lookupBean(ActiveUserProvider.class).getInstance().setActiveUser(null);
      }
    });
  }
  
  @Test
  public void testAnybodyServiceNotblocked() throws Exception {
    final Counter callbackCounter = new Counter();
    helper(new Runnable() {
      @Override
      public void run() {
        restCallHelper(new CountingCallback(callbackCounter), null).anybody();
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
    final RemoteCallback<Void> callback = new CountingCallback(callbackCounter);
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
    final RemoteCallback<Void> callback = new CountingCallback(callbackCounter);
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
    final RemoteCallback<Void> callback = new CountingCallback(callbackCounter);
    final User user = new User("user");
    user.setRoles(new ArrayList<Role>());

    helper(new Runnable() {
      @Override
      public void run() {
        IOC.getBeanManager().lookupBean(ActiveUserProvider.class).getInstance().setActiveUser(user);
        restCallHelper(callback, null).user();
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
  public void testAdminBlockedWhenNotLoggedInAsUser() throws Exception {
    final Counter callbackCounter = new Counter();
    final RemoteCallback<Void> callback = new CountingCallback(callbackCounter);
    final Counter errorCounter = new Counter();
    final RestErrorCallback errorCallback = new RestErrorCountingCallback(errorCounter, UnauthorizedException.class);
    final User user = new User("user");
    user.setRoles(new ArrayList<Role>());

    helper(new Runnable() {
      @Override
      public void run() {
        IOC.getBeanManager().lookupBean(ActiveUserProvider.class).getInstance().setActiveUser(user);
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
    delayTestFinish(2 * (int) TIME_LIMIT);
    CDI.addPostInitTask(test);
  }

  private static SecureRestService restCallHelper(final RemoteCallback<Void> callback,
          final RestErrorCallback errorCallback) {
    final Caller<SecureRestService> caller = IOC.getBeanManager().lookupBean(RestSecurityTestModule.class).getInstance().restCaller;
    
    if (errorCallback != null) {
      return caller.call(callback, errorCallback);
    }
    else {
      return caller.call(callback);
    }
  }

}
