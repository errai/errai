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

package org.jboss.errai.cdi.injection.client.test;

import org.jboss.errai.cdi.injection.client.ReachabiltyInferredDependentBean;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Ignore;

/**
 * This tests an experimental feature of Errai -- relying on reachability analysis to determine whether or not
 * a bean should be accessible through the bean manager as a dependent scoped bean.
 * <p>
 * You <strong>MUST</strong> ensure that the flag <tt>-Derrai.ioc.experimental.infer_dependent_by_reachability=true</tt>
 * is specified for whatever forks the JVM to run these tests. This cannot currently be specified within the test as
 * this test is GWT translatable code.
 *
 * @author Mike Brock
 */
@Ignore
public class ExperimentalDependentScopeTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }


  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testInferredDependentBeanCanBeLookedUp() {
    assertNotNull(IOC.getBeanManager().lookupBean(ReachabiltyInferredDependentBean.class));
  }
}
