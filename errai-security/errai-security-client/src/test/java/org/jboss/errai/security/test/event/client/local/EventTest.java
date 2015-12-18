/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.test.event.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;

public class EventTest extends AbstractErraiCDITest {

  private static final String NAME = "someUserName";
  private static final User SOME_USER = new UserImpl(NAME);

  private EventTestModule module;
  private SecurityContext context;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.test.event.EventTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    module = IOC.getBeanManager().lookupBean(EventTestModule.class).getInstance();
    context = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();
    context.setCachedUser(User.ANONYMOUS);
    module.reset();
  }


  public void testLoginEventFiredOnLogin() throws Exception {
    assertEquals("Precondition failed.", 0, module.getLogoutEvents());
    assertEquals("Precondition failed.", 0, module.getLoginEvents());

    context.setCachedUser(SOME_USER);
    assertEquals(0, module.getLogoutEvents());
    assertEquals(1, module.getLoginEvents());
  }

  public void testLogoutEventFiredOnLogout() throws Exception {
    try {
      testLoginEventFiredOnLogin();
    } catch (AssertionError e) {
      throw new AssertionError("Precondition failed.", e);
    }

    module.reset();
    context.setCachedUser(User.ANONYMOUS);
    assertEquals(1, module.getLogoutEvents());
    assertEquals(0, module.getLoginEvents());
  }

}
