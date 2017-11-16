/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;

import org.jboss.errai.cdi.common.client.qualifier.WithClazzArray;
import org.jboss.errai.cdi.event.client.ClientQualifiedMemberEventObserver;
import org.jboss.errai.cdi.event.client.ClientQualifiedMemberEventProducer;
import org.jboss.errai.cdi.event.client.FiredQualifierObserver;
import org.jboss.errai.cdi.event.client.QualifierMemberTestHelper;
import org.jboss.errai.cdi.event.client.shared.QualifiedMemberEventProducer;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ClientOnlyQualifierMemberIntegrationTest extends AbstractQualifierMemberIntegrationTest {

  @Override
  protected FiredQualifierObserver getQualifierObserver() {
    return IOC.getBeanManager().lookupBean(FiredQualifierObserver.class).getInstance();
  }

  @Override
  protected QualifiedMemberEventProducer getEventProducer() {
    return IOC.getBeanManager().lookupBean(ClientQualifiedMemberEventProducer.class).getInstance();
  }

  @Override
  protected void setup(Runnable run) {
    IOC.getBeanManager().lookupBean(ClientQualifiedMemberEventObserver.class).getInstance();
    IOC.getBeanManager().lookupBean(QualifierMemberTestHelper.class).getInstance().setActive(false, run);
  }

  public void testClazzArrayMember() throws Exception {
    delayTestFinish(DURATION);
    CDI.addPostInitTask(() -> {
      final QualifiedMemberEventProducer producer = getEventProducer();
      final FiredQualifierObserver observer = getQualifierObserver();

      assertEquals(0, observer.observedQualifiers.size());

      assertUntil(DURATION - 1000,
                  () -> producer.fireClazzArray(),
                  () -> {
                    assertEquals(1, observer.observedQualifiers.size());
                    assertEquals(WithClazzArray.class.getName(), observer.observedQualifiers.get(0).getAnnoType());
                    assertEquals(Arrays.toString(new Class<?>[] {Object.class, Class.class}), observer.observedQualifiers.get(0).getValues().get("value"));
                  });
    });
  }

}
