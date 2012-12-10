package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithTransitionA;

import com.google.gwt.user.client.ui.Anchor;

public class TransitionTest extends AbstractErraiCDITest {

  private IOCBeanManager beanManager = IOC.getBeanManager();

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.TransitionTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
  }

  public void testLinkHrefIsSet() throws Exception {
    PageWithTransitionA page = beanManager.lookupBean(PageWithTransitionA.class).getInstance();
    Anchor linkToB = page.linkToB;

    // The 'href' attribute in the template is "otherpage.html" - but the framework
    // should have replaced that with #page_b, which is the path attribute of the
    // @Page annotation in the PageB class.
    assertNotNull(linkToB);
    assertTrue("Expected the linkToB anchor to end with #page_b (the 'path' attribute on the @Page annotation on the PageB class)", 
            linkToB.getHref().endsWith("#page_b"));
  }
}
