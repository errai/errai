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

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.res.BeanWithInjectedUser;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;


public class InjectableUserIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityTest";
  }

  public void testHandlerInitializesMarshallingSystem() throws Exception {
    assertNotNull(ParserFactory.get());
  }

  public void testUserIsInjectable() throws Exception {
    asyncTest();

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final SecurityContext securityContext = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();
        final BeanWithInjectedUser bean = IOC.getBeanManager().lookupBean(BeanWithInjectedUser.class).getInstance();

        // ensure we're starting with a clean slate
        assertEquals(User.ANONYMOUS, securityContext.getCachedUser());
        assertEquals(User.ANONYMOUS, bean.getUser());

        User su = new UserImpl("su2000");
        securityContext.setCachedUser(su);

        final BeanWithInjectedUser bean2 = IOC.getBeanManager().lookupBean(BeanWithInjectedUser.class).getInstance();
        assertEquals(su, bean2.getUser());
        finishTest();
      }
    });
  }

}
