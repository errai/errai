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

package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.testpages.NonCompositePage;
import org.jboss.errai.ui.nav.client.local.testpages.PageA;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithUniqueRole;

public class TransitionToRoleTest extends AbstractErraiCDITest {

  private TransitionToRoleTestApp testApp;
  private Navigation navigation;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    testApp = IOC.getBeanManager().lookupBean(TransitionToRoleTestApp.class).getInstance();
    navigation = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
  }

  public void testNavigationToPageRoleWithTransition() throws Exception {
    navigation.goTo("");
    assertEquals(PageA.class, navigation.currentPage.contentType());

    testApp.testPage.transition.go();

    assertEquals(PageWithUniqueRole.class, navigation.currentPage.contentType());
  }

  public void testNavigationToNonCompositePageByRole() throws Exception {
    navigation.goTo("");
    assertEquals(PageA.class, navigation.currentPage.contentType());

    testApp.testPage.nonCompositeTransition.go();

    assertEquals(NonCompositePage.class, navigation.currentPage.contentType());
  }

}
