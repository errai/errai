package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithTransitionAnchor;

public class TransitionAnchorTest extends AbstractErraiCDITest {

  private IOCBeanManager beanManager = null;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    beanManager = IOC.getBeanManager();
  }

  public void testTransitionAnchorInjection() throws Exception {
    PageWithTransitionAnchor page = beanManager.lookupBean(PageWithTransitionAnchor.class).getInstance();
    assertNotNull(page);
    assertNotNull(page.linkToB.getHref());
    assertTrue(page.linkToB.getHref().endsWith("#page_b"));
  }

}
