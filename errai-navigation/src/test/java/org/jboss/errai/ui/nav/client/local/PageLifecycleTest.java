package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.testpages.PageA;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithExtraState;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithInheritedLifecycleMethods;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithLifecycleMethods;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithPageShowingHistoryTokenMethod;

import com.google.common.collect.ImmutableMultimap;

public class PageLifecycleTest extends AbstractErraiCDITest {

  private IOCBeanManager beanManager = IOC.getBeanManager();
  private Navigation navigation;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    navigation = beanManager.lookupBean(Navigation.class).getInstance();
  }

  public void testPageShowingMethodCalled() throws Exception {
    PageWithLifecycleMethods page = beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance();
    page.beforeShowCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.beforeShowCallCount);
    assertEquals("foo", page.stateWhenBeforeShowWasCalled);
  }

  public void testPageHidingMethodCalled() throws Exception {
    PageWithLifecycleMethods page = beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance();

    // set up by ensuring we're at some other page to start with
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    page.beforeHideCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));
    assertEquals(0, page.beforeHideCallCount);

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, page.beforeHideCallCount);
  }

  public void testPageWithInheritedLifecycleMethods() throws Exception {
    PageWithInheritedLifecycleMethods page = beanManager.lookupBean(PageWithInheritedLifecycleMethods.class).getInstance();
    page.beforeShowCallCount = 0;
    page.beforeHideCallCount = 0;

    navigation.goTo(PageWithInheritedLifecycleMethods.class, ImmutableMultimap.of("state", "inheritedfoo"));

    assertEquals(1, page.beforeShowCallCount);
    assertEquals(0, page.beforeHideCallCount);
    //assertEquals("inheritedfoo", page.stateWhenBeforeShowWasCalled);

    // navigate away to test for pageHiding()
    navigation.goTo(PageA.class, ImmutableMultimap.<String,String>of());

    assertEquals(1, page.beforeShowCallCount);
    assertEquals(1, page.beforeHideCallCount);
  }

  public void testPageShowingMethodWithHistoryTokenParam() throws Exception {
    PageWithPageShowingHistoryTokenMethod page = beanManager.lookupBean(PageWithPageShowingHistoryTokenMethod.class).getInstance();
    assertNull(page.mostRecentStateToken);
    assertEquals(0, page.beforeShowCallCount);

    navigation.goTo(PageWithPageShowingHistoryTokenMethod.class, ImmutableMultimap.of("state", "footastic"));
    assertEquals(1, page.beforeShowCallCount);

    HistoryToken expectedToken = HistoryToken.of("PageWithPageShowingHistoryTokenMethod", ImmutableMultimap.of("state", "footastic"));
    assertEquals(expectedToken, page.mostRecentStateToken);
  }
}
