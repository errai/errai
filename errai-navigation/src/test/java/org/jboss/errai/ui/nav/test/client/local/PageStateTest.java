package org.jboss.errai.ui.nav.test.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.test.client.local.testpages.PageWithExtraState;

import com.google.common.collect.ImmutableMultimap;

public class PageStateTest extends AbstractErraiCDITest {

  private IOCBeanManager beanManager = IOC.getBeanManager();
  private Navigation navigation;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.test.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    navigation = beanManager.lookupBean(Navigation.class).getInstance();
  }

  public void testPassAllStateTokens() throws Exception {
    PageWithExtraState page = beanManager.lookupBean(PageWithExtraState.class).getInstance();
    assertNull(page.getStringThing());
    assertNull(page.getIntThing());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.of(
            "stringThing", "string",
            "intThing", "int"));

    assertEquals("string", page.getStringThing());
    assertEquals("int", page.getIntThing());
  }
}
