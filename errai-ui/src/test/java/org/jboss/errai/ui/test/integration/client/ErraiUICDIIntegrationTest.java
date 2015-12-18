/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.test.integration.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.test.integration.client.res.AppScopedTemplatedBean;
import org.jboss.errai.ui.test.integration.client.res.LazyTestHelper;
import org.jboss.errai.ui.test.integration.client.res.NestedAppScopedTemplatedBean;
import org.jboss.errai.ui.test.integration.client.res.TestAppBean;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Mike Brock
 */
public class ErraiUICDIIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.test.integration.Test";
  }

  public void testErraiUIBeanPropertyConstructedWithSelfProducedDependency() {
    final TestAppBean bean = IOC.getBeanManager().lookupBean(TestAppBean.class).getInstance();

    assertNotNull(bean);
    assertNotNull(bean.getW());
  }

  public void testAppScopedTemplatedBeanIsLoadedWhenAddedToWidget() throws Exception {
    final LazyTestHelper testHelper = IOC.getBeanManager().lookupBean(LazyTestHelper.class).getInstance();
    assertEquals("Precondition failed: the templated bean should not have been loaded yet.", 0, AppScopedTemplatedBean.postConstructCount);
    assertEquals("Precondition failed: the nested templated bean should not have been loaded yet.", 0, NestedAppScopedTemplatedBean.postConstructCount);

    RootPanel.get().add(testHelper.bean);
    assertEquals("The templated bean should have been loaded after being added to the RootPanel.", 1, AppScopedTemplatedBean.postConstructCount);
    assertEquals("The nested templated bean should have been loaded after being added to the RootPanel.", 1, NestedAppScopedTemplatedBean.postConstructCount);
  }
}
