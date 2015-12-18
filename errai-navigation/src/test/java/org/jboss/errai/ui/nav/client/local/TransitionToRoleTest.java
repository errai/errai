package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
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

}
