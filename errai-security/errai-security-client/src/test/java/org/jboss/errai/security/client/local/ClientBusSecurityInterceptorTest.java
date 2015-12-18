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

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.res.ClientInterceptorTestAssistant;
import org.jboss.errai.security.client.local.res.Counter;
import org.jboss.errai.security.client.shared.AdminService;
import org.jboss.errai.security.client.shared.AuthenticatedService;
import org.jboss.errai.security.client.shared.DiverseService;
import org.jboss.errai.security.shared.api.identity.User;

public class ClientBusSecurityInterceptorTest extends BusSecurityInterceptorTest {

  @Override
  protected void postLogout() {
  }

  @Override
  protected void postLogin(User user) {
  }

  @Override
  protected void gwtSetUp() throws Exception {
    ClientInterceptorTestAssistant.active = true;
    super.gwtSetUp();
  }

  public void testAuthInterceptorRedirectsWithNoErrorHandler() throws Exception {
    asyncTest();
    runNavTest(new Runnable() {
      @Override
      public void run() {
        final TestLoginPage page = IOC.getBeanManager().lookupBean(TestLoginPage.class).getInstance();
        assertEquals(0, page.getPageLoadCounter());

        // Invalidate cache
        provider.setUser(User.ANONYMOUS);
        assertTrue(provider.isValid());
        createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, AuthenticatedService.class).userStuff();
        testUntil(TIME_LIMIT, new Runnable() {
          @Override
          public void run() {
            assertEquals(1, page.getPageLoadCounter());
          }
        });
      }
    });
  }

  public void testRoleInterceptorRedirectsToLoginWhenNotLoggedInAndNoErrorHandler() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    runNavTest(new Runnable() {
      @Override
      public void run() {
        final TestLoginPage page = IOC.getBeanManager().lookupBean(TestLoginPage.class).getInstance();
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

  public void testRoleInterceptorLoggedInUnprivelegedRedirectsToSecurityErrorWithNoErrorCallback() throws Exception {
    asyncTest();
    final Counter counter = new Counter();
    runNavTest(new Runnable() {
      @Override
      public void run() {
        final TestSecurityErrorPage page = IOC.getBeanManager().lookupBean(TestSecurityErrorPage.class).getInstance();
        assertEquals(0, page.getPageLoadCounter());

        afterLogin("john", "123", new RemoteCallback<User>() {
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
        });
      }
    });
  }

}
