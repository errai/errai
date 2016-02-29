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

import org.jboss.errai.cdi.event.client.ClientQualifiedMemberEventProducer;
import org.jboss.errai.cdi.event.client.FiredQualifierObserver;
import org.jboss.errai.cdi.event.client.QualifierMemberTestHelper;
import org.jboss.errai.cdi.event.client.shared.QualifiedMemberEventProducer;
import org.jboss.errai.ioc.client.container.IOC;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ClientToServerQualifierMemberIntegrationTest extends AbstractQualifierMemberIntegrationTest {

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
    IOC.getBeanManager().lookupBean(QualifierMemberTestHelper.class).getInstance().setActive(true, run);
  }

}
