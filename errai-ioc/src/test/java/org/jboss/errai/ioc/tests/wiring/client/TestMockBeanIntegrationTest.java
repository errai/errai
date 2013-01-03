/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.tests.wiring.client;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.res.MockProductionBean;
import org.jboss.errai.ioc.tests.wiring.client.res.ProductionBeanDependentBean;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class TestMockBeanIntegrationTest extends AbstractErraiIOCTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  public void testMockedBeanInjection() throws Exception {

    ProductionBeanDependentBean bean = IOC.getBeanManager()
        .lookupBean(ProductionBeanDependentBean.class).getInstance();

    assertNotNull(bean);
    assertNotNull(bean.getMockableCommonInterface());
    assertTrue("Expected MockProductionBean; wrong bean injected",
        bean.getMockableCommonInterface() instanceof MockProductionBean);
  }

  public void testMockedCallerInjection() throws Exception {
    ProductionBeanDependentBean bean = IOC.getBeanManager()
        .lookupBean(ProductionBeanDependentBean.class).getInstance();

    assertNotNull(bean);
    assertNotNull(bean.getMockableCaller());
    bean.getMockableCaller().call(new RemoteCallback<Boolean>() {
      @Override
      public void callback(Boolean response) {
        // response should come from HappyServiceMockedCallerProvider
        assertTrue(response);
      }
    }).isHappy();
  }
}