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

package org.jboss.errai.cdi.event.client.test;

import static org.jboss.errai.ioc.client.container.IOC.getBeanManager;

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.cdi.event.client.SubTypeObserverModule;
import org.jboss.errai.cdi.event.client.IfaceBImpl;
import org.jboss.errai.cdi.event.client.LocalB;
import org.jboss.errai.cdi.event.client.LocalC;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;

/**
 * @author Max Barkely <mbarkley@redhat.com>
 */
public class ClientEventSuperTypeTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
  }

  public void testObservingDirectSubtype() throws Exception {
    final SubTypeObserverModule observer = getBeanManager().lookupBean(SubTypeObserverModule.class).getInstance();
    final List<LocalB> fired = Arrays.asList(new LocalB(), new LocalB());
    observer.eventA.fire(fired.get(0));
    observer.eventB.fire(fired.get(1));

    assertEquals(fired, observer.observed);
  }

  public void testObservingIndirectSubtype() throws Exception {
    final SubTypeObserverModule observer = getBeanManager().lookupBean(SubTypeObserverModule.class).getInstance();
    final List<LocalC> fired = Arrays.asList(new LocalC(), new LocalC(), new LocalC());
    observer.eventA.fire(fired.get(0));
    observer.eventB.fire(fired.get(1));
    observer.eventC.fire(fired.get(2));

    assertEquals(fired, observer.observed);
  }

  public void testObservingIndirectIfaceSubtype() throws Exception {
    final SubTypeObserverModule observer = getBeanManager().lookupBean(SubTypeObserverModule.class).getInstance();
    final List<IfaceBImpl> fired = Arrays.asList(new IfaceBImpl());
    observer.eventImplB.fire(fired.get(0));

    assertEquals(fired, observer.observed);
  }

}
