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

import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.google.gwt.user.client.Cookies;

/**
 * Tests for proper behaviour when the app loads up and the remembered user
 * cookie is already populated. See also
 * {@link UnpopulatedUserStorageIntegrationTest} for tests where the cookie is
 * not set when the page initially loads.
 */
public class PrePopulatedUserStorageIntegrationTest extends AbstractSecurityInterceptorTest {

  private User prePopulatedUser;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    MarshallerFramework.initializeDefaultSessionProvider();
    prePopulatedUser = new UserImpl("remembered");

    // must ensure we are logged out on the server side before any @AfterInitialization methods run
    // (previous tests may have logged in)
    InitVotes.waitFor(PrePopulatedUserStorageIntegrationTest.class);
    InitVotes.registerOneTimeDependencyCallback(ClientMessageBus.class, new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void x) {
            InitVotes.voteFor(PrePopulatedUserStorageIntegrationTest.class);
          }
        }, AuthenticationService.class).logout();
      }
    });

    // now fake a remembered client-side user from a previous session
    Cookies.setCookie(UserCookieEncoder.USER_COOKIE_NAME, Marshalling.toJSON(prePopulatedUser));

    super.gwtSetUp();
  }

  public void testUsingRememberedUserOnAppStart() throws Exception {
    final SecurityContext securityContext = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();
    assertEquals(prePopulatedUser, securityContext.getCachedUser());
  }

  public void testGracefulFailureWithRememberedUserButInvalidServerSession() throws Exception {
    asyncTest();

    final SecurityContext securityContext = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();
    assertEquals(prePopulatedUser, securityContext.getCachedUser());

    // this is a new HTTP session, so we're not actually logged in on the server.
    // once the security service's RPC call comes back, the client-side context should agree that we're not logged in
    testUntil(TIME_LIMIT, new Runnable() {

      @Override
      public void run() {
        assertEquals(User.ANONYMOUS, securityContext.getCachedUser());
        assertNull(Cookies.getCookie(UserCookieEncoder.USER_COOKIE_NAME));
        finishTest();
      }
    });
  }

}
