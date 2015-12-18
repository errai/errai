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

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;

/**
 * Tests for proper behaviour when the app loads up and the remembered user
 * cookie is not present. See also
 * {@link PrePopulatedUserStorageIntegrationTest} for tests where the cookie is
 * set when the page initially loads.
 */
public class UnpopulatedUserStorageIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    assertTrue(Cookies.isCookieEnabled());
    Cookies.removeCookie(UserCookieEncoder.USER_COOKIE_NAME);
    super.gwtSetUp();
  }

  public void testRememberUserAfterSuccessfulLogin() throws Exception {
    asyncTest();

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final SecurityContext securityContext = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();

        // ensure we're starting with a clean slate
        assertEquals(User.ANONYMOUS, securityContext.getCachedUser());

        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(response, securityContext.getCachedUser());
            String expectedCookieValue = Marshalling.toJSON(response);
            assertEquals(expectedCookieValue, Cookies.getCookie(UserCookieEncoder.USER_COOKIE_NAME));
            finishTest();
          }
        }, AuthenticationService.class).login("user", "password");
      }
    });
  }

  // NOTE: the 500ms delay in the logout polling timer is critical to the success of this test,
  //       because if we log out immediately from within the login success callback, the SecurityContextImpl's
  //       own initial getUser() request to the server may come back AFTER our logout() call!
  //       See ERRAI-728 for details.
  public void testForgetUserAfterSuccessfulLogout() throws Exception {
    asyncTest();

    class WorkerSharedState {
      boolean loginCompleted = false;
    }
    final WorkerSharedState sharedState = new WorkerSharedState();

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final SecurityContext securityContext = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();

        // ensure we're starting with a clean slate
        assertEquals(User.ANONYMOUS, securityContext.getCachedUser());

        // perform login
        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {

            // unblock the timer that's waiting to do the logout
            sharedState.loginCompleted = true;
          }
        }, AuthenticationService.class).login("user", "password");

        // poll until login finishes, then log out
        new Timer() {

          @Override
          public void run() {
            if (sharedState.loginCompleted) {
              MessageBuilder.createCall(new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                  assertEquals(User.ANONYMOUS, securityContext.getCachedUser());
                  assertNull(Cookies.getCookie(UserCookieEncoder.USER_COOKIE_NAME));
                  finishTest();
                }
              }, AuthenticationService.class).logout();
            }
            else {
              schedule(500);
            }
          }
        }.schedule(500);
      }
    });
  }

}
