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
    assertEquals(0, page.getIntThing());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.of(
            "stringThing", "string",
            "intThing", "123"));

    assertEquals("string", page.getStringThing());
    assertEquals(123, page.getIntThing());
  }

  /**
   * If there are multiple values for the same key, but the corresponding
   * {@code @PageState} field in the page is not a collection, the field should
   * get the <i>first</i> value for its key.
   */
  public void testScalarGetsFirstValueInToken() throws Exception {
    PageWithExtraState page = beanManager.lookupBean(PageWithExtraState.class).getInstance();
    assertNull(page.getStringThing());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.of(
            "nonexistent", "====",
            "stringThing", "string0",
            "stringThing", "string1",
            "stringThing", "string2"));

    assertEquals("string0", page.getStringThing());
  }

}
