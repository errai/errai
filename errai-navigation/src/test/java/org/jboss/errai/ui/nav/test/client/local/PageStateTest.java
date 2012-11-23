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
    assertEquals((byte) 0, page.getByteThing());
    assertEquals((short) 0, page.getShortThing());
    assertEquals(0, page.getIntThing());
    assertEquals(0L, page.getLongThing());
    assertEquals(0f, page.getFloatThing(), 0f);
    assertEquals(0.0, page.getDoubleThing(), 0.0);
    assertEquals(false, page.getBoolThing());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String,String>builder()
            .put("stringThing", "string")
            .put("byteThing", "12")
            .put("shortThing", "123")
            .put("intThing", "1234")
            .put("longThing", "12345")
            .put("floatThing", "1.2")
            .put("doubleThing", "1.23")
            .put("boolThing", "true")
            .build());

    assertEquals("string", page.getStringThing());
    assertEquals((byte) 12, page.getByteThing());
    assertEquals((short) 123, page.getShortThing());
    assertEquals(1234, page.getIntThing());
    assertEquals(12345L, page.getLongThing());
    assertEquals(1.2f, page.getFloatThing(), 0f);
    assertEquals(1.23, page.getDoubleThing(), 0.0);
    assertEquals(true, page.getBoolThing());
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
