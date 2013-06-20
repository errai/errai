package org.jboss.errai.ui.nav.client.local;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.nav.client.local.testpages.PageA;
import org.jboss.errai.ui.nav.client.local.testpages.PageAWithRedirect;
import org.jboss.errai.ui.nav.client.local.testpages.PageBWithRedirect;
import org.jboss.errai.ui.nav.client.local.testpages.PageC;
import org.jboss.errai.ui.nav.client.local.testpages.PageCWithRedirect;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithDoubleRedirect;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithException;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithExtraState;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithInheritedLifecycleMethods;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithLifecycleMethods;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithPageShowingHistoryTokenMethod;

import static org.jboss.errai.ui.nav.client.local.testpages.BasePageForLifecycleTracing.lifecycleTracer;

public class PageLifecycleTest extends AbstractErraiCDITest {

  private SyncBeanManager beanManager = IOC.getBeanManager();
  private Navigation navigation;
  private HandlerRegistration historyHandlerRegistration;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    History.newItem(""); // Ensure initial History token
    lifecycleTracer.clear();
    disableBus = true;
    super.gwtSetUp();
    navigation = beanManager.lookupBean(Navigation.class).getInstance();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    // Each unit test creates a new Navigation instance and installs another HistoryHandler
    // however ApplicationScoped scoped beans can not be destroyed
    // Clean the handler manually because multiple HistoryHandlers interfere with tests
    navigation.cleanUp();
    super.gwtTearDown();
  }

  public void testPageShowingMethodCalled() throws Exception {
    PageWithLifecycleMethods page = beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance();
    page.beforeShowCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.beforeShowCallCount);
    assertEquals("foo", page.stateWhenBeforeShowWasCalled);
  }

  public void testPageShownMethodCalled() throws Exception {
    PageWithLifecycleMethods page = beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance();
    page.afterShowCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.afterShowCallCount);
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

  public void testPageHiddenMethodCalled() throws Exception {
    PageWithLifecycleMethods page = beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance();

    // set up by ensuring we're at some other page to start with
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    page.afterHideCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));
    assertEquals(0, page.afterHideCallCount);

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, page.afterHideCallCount);
  }

  public void testPageWithInheritedLifecycleMethods() throws Exception {
    PageWithInheritedLifecycleMethods page = beanManager.lookupBean(PageWithInheritedLifecycleMethods.class).getInstance();
    page.beforePageShowCallCount = 0;
    page.afterPageShowCallCount = 0;
    page.beforePageHideCallCount = 0;
    page.afterPageHideCallCount = 0;

    navigation.goTo(PageWithInheritedLifecycleMethods.class, ImmutableMultimap.of("inheritedState", "inheritedfoo"));

    assertEquals(1, page.beforePageShowCallCount);
    assertEquals(1, page.afterPageShowCallCount);
    assertEquals(0, page.beforePageHideCallCount);
    assertEquals(0, page.afterPageHideCallCount);
    assertEquals("inheritedfoo", page.stateWhenBeforeShowWasCalled);

    // navigate away to test for pageHiding()
    navigation.goTo(PageA.class, ImmutableMultimap.<String,String>of());

    assertEquals(1, page.beforePageShowCallCount);
    assertEquals(1, page.afterPageShowCallCount);
    assertEquals(1, page.beforePageHideCallCount);
    assertEquals(1, page.afterPageHideCallCount);
  }

  public void testPageShowingMethodWithHistoryTokenParam() throws Exception {
    PageWithPageShowingHistoryTokenMethod page = beanManager.lookupBean(PageWithPageShowingHistoryTokenMethod.class).getInstance();
    assertNull(page.mostRecentStateToken);
    assertEquals(0, page.beforeShowCallCount);
    assertEquals(0, page.afterShowCallCount);

    navigation.goTo(PageWithPageShowingHistoryTokenMethod.class, ImmutableMultimap.of("state", "footastic"));
    assertEquals(1, page.beforeShowCallCount);
    assertEquals(1, page.afterShowCallCount);

    HistoryToken expectedToken = HistoryToken.of("PageWithPageShowingHistoryTokenMethod", ImmutableMultimap.of("state", "footastic"));
    assertEquals(expectedToken, page.mostRecentStateToken);
  }

  public void testRedirect() {
    PageAWithRedirect pageA = beanManager.lookupBean(PageAWithRedirect.class).getInstance();
    PageBWithRedirect pageB = beanManager.lookupBean(PageBWithRedirect.class).getInstance();
    PageCWithRedirect pageC = beanManager.lookupBean(PageCWithRedirect.class).getInstance();

    pageA.redirectPage = PageBWithRedirect.class;
    pageB.redirectPage = PageCWithRedirect.class;
    pageC.redirectPage = null;

    navigation.goTo(PageAWithRedirect.class, ImmutableMultimap.<String, String>of());

    assertEquals("PageCWithRedirect", History.getToken());
    assertEquals(10, lifecycleTracer.size());

    assertEquals(PageShowing.class, lifecycleTracer.get(0).lifecycleAnnotation);
    assertEquals(PageAWithRedirect.class, lifecycleTracer.get(0).page);

    assertEquals(PageShown.class, lifecycleTracer.get(1).lifecycleAnnotation);
    assertEquals(PageAWithRedirect.class, lifecycleTracer.get(1).page);

    assertEquals(PageHiding.class, lifecycleTracer.get(2).lifecycleAnnotation);
    assertEquals(PageAWithRedirect.class, lifecycleTracer.get(2).page);

    assertEquals(PageHidden.class, lifecycleTracer.get(3).lifecycleAnnotation);
    assertEquals(PageAWithRedirect.class, lifecycleTracer.get(3).page);

    assertEquals(PageShowing.class, lifecycleTracer.get(4).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(4).page);

    assertEquals(PageShown.class, lifecycleTracer.get(5).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(5).page);

    assertEquals(PageHiding.class, lifecycleTracer.get(6).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(6).page);

    assertEquals(PageHidden.class, lifecycleTracer.get(7).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(7).page);

    assertEquals(PageShowing.class, lifecycleTracer.get(8).lifecycleAnnotation);
    assertEquals(PageCWithRedirect.class, lifecycleTracer.get(8).page);

    assertEquals(PageShown.class, lifecycleTracer.get(9).lifecycleAnnotation);
    assertEquals(PageCWithRedirect.class, lifecycleTracer.get(9).page);
  }

  public void testDoubleRedirect() {
    PageWithDoubleRedirect pageA = beanManager.lookupBean(PageWithDoubleRedirect.class).getInstance();
    PageBWithRedirect pageB = beanManager.lookupBean(PageBWithRedirect.class).getInstance();
    PageCWithRedirect pageC = beanManager.lookupBean(PageCWithRedirect.class).getInstance();

    pageA.redirectPage = PageBWithRedirect.class;
    pageA.secondRedirectPage = PageCWithRedirect.class;
    pageB.redirectPage = null;
    pageC.redirectPage = null;

    navigation.goTo(PageWithDoubleRedirect.class, ImmutableMultimap.<String, String>of());

    assertEquals("PageCWithRedirect", History.getToken());
    assertEquals(10, lifecycleTracer.size());

    assertEquals(PageShowing.class, lifecycleTracer.get(0).lifecycleAnnotation);
    assertEquals(PageWithDoubleRedirect.class, lifecycleTracer.get(0).page);

    assertEquals(PageShown.class, lifecycleTracer.get(1).lifecycleAnnotation);
    assertEquals(PageWithDoubleRedirect.class, lifecycleTracer.get(1).page);

    assertEquals(PageHiding.class, lifecycleTracer.get(2).lifecycleAnnotation);
    assertEquals(PageWithDoubleRedirect.class, lifecycleTracer.get(2).page);

    assertEquals(PageHidden.class, lifecycleTracer.get(3).lifecycleAnnotation);
    assertEquals(PageWithDoubleRedirect.class, lifecycleTracer.get(3).page);

    assertEquals(PageShowing.class, lifecycleTracer.get(4).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(4).page);

    assertEquals(PageShown.class, lifecycleTracer.get(5).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(5).page);

    assertEquals(PageHiding.class, lifecycleTracer.get(6).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(6).page);

    assertEquals(PageHidden.class, lifecycleTracer.get(7).lifecycleAnnotation);
    assertEquals(PageBWithRedirect.class, lifecycleTracer.get(7).page);

    assertEquals(PageShowing.class, lifecycleTracer.get(8).lifecycleAnnotation);
    assertEquals(PageCWithRedirect.class, lifecycleTracer.get(8).page);

    assertEquals(PageShown.class, lifecycleTracer.get(9).lifecycleAnnotation);
    assertEquals(PageCWithRedirect.class, lifecycleTracer.get(9).page);
  }

  public void testRedirectOnHistoryChange() {
    PageAWithRedirect pageA = beanManager.lookupBean(PageAWithRedirect.class).getInstance();
    PageBWithRedirect pageB = beanManager.lookupBean(PageBWithRedirect.class).getInstance();

    pageA.redirectPage = PageBWithRedirect.class;
    pageB.redirectPage = null;

    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        historyHandlerRegistration.removeHandler();

        assertEquals("PageBWithRedirect", History.getToken());
        assertEquals(6, lifecycleTracer.size());

        assertEquals(PageShowing.class, lifecycleTracer.get(0).lifecycleAnnotation);
        assertEquals(PageAWithRedirect.class, lifecycleTracer.get(0).page);

        assertEquals(PageShown.class, lifecycleTracer.get(1).lifecycleAnnotation);
        assertEquals(PageAWithRedirect.class, lifecycleTracer.get(1).page);

        assertEquals(PageHiding.class, lifecycleTracer.get(2).lifecycleAnnotation);
        assertEquals(PageAWithRedirect.class, lifecycleTracer.get(2).page);

        assertEquals(PageHidden.class, lifecycleTracer.get(3).lifecycleAnnotation);
        assertEquals(PageAWithRedirect.class, lifecycleTracer.get(3).page);

        assertEquals(PageShowing.class, lifecycleTracer.get(4).lifecycleAnnotation);
        assertEquals(PageBWithRedirect.class, lifecycleTracer.get(4).page);

        assertEquals(PageShown.class, lifecycleTracer.get(5).lifecycleAnnotation);
        assertEquals(PageBWithRedirect.class, lifecycleTracer.get(5).page);

        finishTest();
      }
    });

    delayTestFinish(5000);
    History.newItem("PageAWithRedirect");
  }

  public void testRedirectLoop() {
    PageAWithRedirect pageA = beanManager.lookupBean(PageAWithRedirect.class).getInstance();
    PageBWithRedirect pageB = beanManager.lookupBean(PageBWithRedirect.class).getInstance();
    PageCWithRedirect pageC = beanManager.lookupBean(PageCWithRedirect.class).getInstance();

    pageA.redirectPage = PageBWithRedirect.class;
    pageB.redirectPage = PageCWithRedirect.class;
    pageC.redirectPage = PageAWithRedirect.class;

    try {
      navigation.goTo(PageAWithRedirect.class, ImmutableMultimap.<String, String>of());
      fail("Expected redirect loop exception");
    } catch (RuntimeException e) {
    }
  }

  public void testPageWithException() {
    PageWithException page = beanManager.lookupBean(PageWithException.class).getInstance();

    try {
      navigation.goTo(PageWithException.class, ImmutableMultimap.<String, String>of());
    } catch (NullPointerException ignored) {
    }

    navigation.goTo(PageA.class, ImmutableMultimap.<String, String>of());

    assertEquals("PageA", History.getToken());
  }

  public void testIt() {
    navigation.goTo(PageC.class, ImmutableMultimap.<String, String>of());
  }

}
