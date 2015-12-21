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

import static org.jboss.errai.ui.nav.client.local.testpages.BasePageForLifecycleTracing.lifecycleTracer;

import org.jboss.errai.common.client.PageRequest;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerGenerator;
import org.jboss.errai.ui.nav.client.local.testpages.ApplicationScopedLifecycleCountingPage;
import org.jboss.errai.ui.nav.client.local.testpages.ApplicationScopedPage;
import org.jboss.errai.ui.nav.client.local.testpages.DependentLifecycleCountingPage;
import org.jboss.errai.ui.nav.client.local.testpages.EntryPointPage;
import org.jboss.errai.ui.nav.client.local.testpages.ExplicitlyDependentScopedPage;
import org.jboss.errai.ui.nav.client.local.testpages.ImplicitlyDependentScopedPage;
import org.jboss.errai.ui.nav.client.local.testpages.NonCompositePage;
import org.jboss.errai.ui.nav.client.local.testpages.NonCompositePageWithLifecycleMethods;
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
import org.jboss.errai.ui.nav.client.local.testpages.SingletonScopedPage;
import org.jboss.errai.ui.nav.client.shared.NavigationEvent;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class PageLifecycleTest extends AbstractErraiCDITest {

  private final SyncBeanManager beanManager = IOC.getBeanManager();
  private Navigation navigation;
  private HandlerRegistration historyHandlerRegistration;
  private HistoryTokenFactory htFactory;

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
    htFactory = beanManager.lookupBean(HistoryTokenFactory.class).getInstance();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    // Each unit test creates a new Navigation instance and installs another HistoryHandler
    // however ApplicationScoped scoped beans can not be destroyed
    // Clean the handler manually because multiple HistoryHandlers interfere with tests
    navigation.cleanUp();
    super.gwtTearDown();
  }

  /**
   * Unlike the rest of the tests in this class, this one test sits around and
   * waits for the default page to show up on its own. This is to check that the
   * default page will not show up until all the init votes are in.
   * <p>
   * (All the other tests just force an immediate page transition, so they
   * bootstrap the navigation system earlier than it would otherwise have done
   * on its own)
   */
  public void testPageNotShownUntilFrameworkInitialized() throws Exception {

    // this thing holds a boolean which will become true as soon as the init votes are all in
    final boolean[] isInitialized = new boolean[1];
    InitVotes.registerOneTimePreInitCallback(new Runnable() {
      @Override
      public void run() {
        isInitialized[0] = true;
      }
    });

    final PageA page = beanManager.lookupBean(PageA.class).getInstance();
    assertEquals("Page was already shown before the test even started!",
            0, page.getBeforeShowCallCount());

    // we give the init state holder to the page so it can capture the init state when it gets its @PageShowing callback
    page.setInitStateHolder(isInitialized);

    // now we poll until the starting page has been shown
    final Timer timer = new Timer() {
      @Override
      public void run() {
        if (page.getBeforeShowCallCount() == 0) {
          // wait longer; the page hasn't been shown yet!
          schedule(500);
          return;
        }

        assertTrue("Starting Page was shown before client bootstrap completed!", page.isInitStateWhenBeforeShowWasCalled());
        finishTest();
      }
    };
    timer.schedule(500);
    delayTestFinish(5000);
  }

  public void testPageShowingMethodCalled() throws Exception {
    PageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance());
    page.beforeShowCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.beforeShowCallCount);
    assertEquals("foo", page.stateWhenBeforeShowWasCalled);
  }

  public void testPageShowingMethodCalledForNonCompositeTemplated() throws Exception {
    NonCompositePageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(NonCompositePageWithLifecycleMethods.class).getInstance());
    assertEquals(0, page.getShowing());

    navigation.goTo(NonCompositePageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.getShowing());
    assertEquals("foo", page.getState());
  }

  public void testPageShownMethodCalled() throws Exception {
    PageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance());
    page.afterShowCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.afterShowCallCount);
  }

  public void testPageShownMethodCalledForNonCompositeTemplated() throws Exception {
    NonCompositePageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(NonCompositePageWithLifecycleMethods.class).getInstance());
    assertEquals(0, page.getShown());

    navigation.goTo(NonCompositePageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    assertEquals(1, page.getShown());
    assertEquals("foo", page.getState());
  }

  public void testPageHidingMethodCalled() throws Exception {
    PageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance());

    // set up by ensuring we're at some other page to start with
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    page.beforeHideCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));
    assertEquals(0, page.beforeHideCallCount);

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, page.beforeHideCallCount);
  }

  public void testPageHidingMethodCalledForNonCompositeTemplated() throws Exception {
    NonCompositePageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(NonCompositePageWithLifecycleMethods.class).getInstance());

    // set up by ensuring we're at some other page to start with
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, page.getHiding());

    navigation.goTo(NonCompositePageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));
    assertEquals(0, page.getHiding());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, page.getHiding());
  }

  public void testPageHiddenMethodCalled() throws Exception {
    PageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithLifecycleMethods.class).getInstance());

    // set up by ensuring we're at some other page to start with
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    page.afterHideCallCount = 0;

    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));
    assertEquals(0, page.afterHideCallCount);

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, page.afterHideCallCount);
  }

  public void testPageHiddenMethodCalledForNonCompositeTemplated() throws Exception {
    NonCompositePageWithLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(NonCompositePageWithLifecycleMethods.class).getInstance());

    // set up by ensuring we're at some other page to start with
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, page.getHidden());

    navigation.goTo(NonCompositePageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));
    assertEquals(0, page.getHidden());

    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, page.getHidden());
  }

  public void testNonCompositeTemplatedDependentScopedPageIsDestroyedAfterHiding() throws Exception {
    NonCompositePage.resetDestroyed();
    assertEquals(0, NonCompositePage.getDestroyed().size());
    navigation.goTo(NonCompositePage.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, NonCompositePage.getDestroyed().size());
    Object nonCompositePage = navigation.currentComponent;

    // go somewhere else; doesn't matter where
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, NonCompositePage.getDestroyed().size());
    assertEquals(nonCompositePage, NonCompositePage.getDestroyed().iterator().next());
  }

  public void testExplicitlyDependentScopedPageIsDestroyedAfterHiding() throws Exception {
    assertEquals(0, ExplicitlyDependentScopedPage.getPreDestroyCallCount());
    navigation.goTo(ExplicitlyDependentScopedPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, ExplicitlyDependentScopedPage.getPreDestroyCallCount());

    // go somewhere else; doesn't matter where
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, ExplicitlyDependentScopedPage.getPreDestroyCallCount());
  }

  public void testImplicitlyDependentScopedPageIsDestroyedAfterHiding() throws Exception {
    assertEquals(0, ImplicitlyDependentScopedPage.getPreDestroyCallCount());
    navigation.goTo(ImplicitlyDependentScopedPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, ImplicitlyDependentScopedPage.getPreDestroyCallCount());

    // go somewhere else; doesn't matter where
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, ImplicitlyDependentScopedPage.getPreDestroyCallCount());
  }

  public void testApplicationScopedPageIsNotDestroyedAfterHiding() throws Exception {
    assertEquals(0, ApplicationScopedPage.getPreDestroyCallCount());
    navigation.goTo(ApplicationScopedPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, ApplicationScopedPage.getPreDestroyCallCount());

    // go somewhere else; doesn't matter where
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, ApplicationScopedPage.getPreDestroyCallCount());
  }

  public void testSingletonScopedPageIsNotDestroyedAfterHiding() throws Exception {
    assertEquals(0, SingletonScopedPage.getPreDestroyCallCount());
    navigation.goTo(SingletonScopedPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, SingletonScopedPage.getPreDestroyCallCount());

    // go somewhere else; doesn't matter where
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, SingletonScopedPage.getPreDestroyCallCount());
  }

  public void testEntryPointPageIsNotDestroyedAfterHiding() throws Exception {
    assertEquals(0, EntryPointPage.getPreDestroyCallCount());
    navigation.goTo(EntryPointPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, EntryPointPage.getPreDestroyCallCount());

    // go somewhere else; doesn't matter where
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.<String, String>of());
    assertEquals(0, EntryPointPage.getPreDestroyCallCount());
  }

  public void testDependentScopedPageIsDestroyedAfterIOCLifecycleRedirect() throws Exception {
    assertEquals(0, DependentLifecycleCountingPage.creationCounter);
    assertEquals(0, DependentLifecycleCountingPage.destructionCounter);

    // Creates a listener to veto navigation to DependentLifecycleCountingPages.
    IOC.registerLifecycleListener(DependentLifecycleCountingPage.class,
            new LifecycleListenerGenerator<DependentLifecycleCountingPage>() {

              @Override
              public LifecycleListener<DependentLifecycleCountingPage> newInstance() {
                return new LifecycleListener<DependentLifecycleCountingPage>() {

                  @Override
                  public void observeEvent(LifecycleEvent<DependentLifecycleCountingPage> event) {
                    if (event.getInstance() instanceof DependentLifecycleCountingPage) {
                      event.veto();
                    }
                  }

                  @Override
                  public boolean isObserveableEventType(
                          Class<? extends LifecycleEvent<DependentLifecycleCountingPage>> eventType) {
                    return Access.class.equals(eventType);
                  }
                };
              }
            });

    navigation.goTo(DependentLifecycleCountingPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(1, DependentLifecycleCountingPage.creationCounter);
    assertEquals(1, DependentLifecycleCountingPage.destructionCounter);
  }

  public void testApplicationScopedPageIsNotDestroyedAfterIOCLifecycleRedirect() throws Exception {
    // Force creation of app scoped bean
    IOC.getBeanManager().lookupBean(ApplicationScopedLifecycleCountingPage.class).getInstance();
    final int creations = ApplicationScopedLifecycleCountingPage.creationCounter;
    final int destructions = ApplicationScopedLifecycleCountingPage.destructionCounter;

    // Creates a listener to veto navigation to DependentLifecycleCountingPages.
    IOC.registerLifecycleListener(ApplicationScopedLifecycleCountingPage.class,
            new LifecycleListenerGenerator<ApplicationScopedLifecycleCountingPage>() {

              @Override
              public LifecycleListener<ApplicationScopedLifecycleCountingPage> newInstance() {
                return new LifecycleListener<ApplicationScopedLifecycleCountingPage>() {

                  @Override
                  public void observeEvent(LifecycleEvent<ApplicationScopedLifecycleCountingPage> event) {
                    if (event.getInstance() instanceof ApplicationScopedLifecycleCountingPage) {
                      event.veto();
                    }
                  }

                  @Override
                  public boolean isObserveableEventType(
                          Class<? extends LifecycleEvent<ApplicationScopedLifecycleCountingPage>> eventType) {
                    return Access.class.equals(eventType);
                  }
                };
              }
            });

    navigation.goTo(ApplicationScopedLifecycleCountingPage.class, ImmutableMultimap.<String, String>of());
    assertEquals(creations, ApplicationScopedLifecycleCountingPage.creationCounter);
    assertEquals(destructions, ApplicationScopedLifecycleCountingPage.destructionCounter);
  }

  public void testPageWithInheritedLifecycleMethods() throws Exception {
    PageWithInheritedLifecycleMethods page = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithInheritedLifecycleMethods.class).getInstance());
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
    PageWithPageShowingHistoryTokenMethod page = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithPageShowingHistoryTokenMethod.class).getInstance());
    assertNull(page.mostRecentStateToken);
    assertEquals(0, page.beforeShowCallCount);
    assertEquals(0, page.afterShowCallCount);

    navigation.goTo(PageWithPageShowingHistoryTokenMethod.class, ImmutableMultimap.of("state", "footastic"));
    assertEquals(1, page.beforeShowCallCount);
    assertEquals(1, page.afterShowCallCount);

    HistoryToken expectedToken = htFactory.createHistoryToken("PageWithPageShowingHistoryTokenMethod", ImmutableMultimap.of("state", "footastic"));
    assertEquals(expectedToken, page.mostRecentStateToken);
  }

  public void testEventRaisedOnPageShown() {
    // given
    PageWithExtraState pageWithExtraState = Factory.maybeUnwrapProxy(beanManager.lookupBean(PageWithExtraState.class).getInstance());

    // when
    navigation.goTo(PageWithLifecycleMethods.class, ImmutableMultimap.of("state", "foo"));

    // then
    final NavigationEvent event = pageWithExtraState.getEvent();
    assertNotNull(event);

    final PageRequest pageRequest = event.getPageRequest();
    assertNotNull(pageRequest);
    assertEquals("PageWithLifecycleMethods", pageRequest.getPageName());
    assertNotNull(pageRequest.getState());
    assertEquals(1, pageRequest.getState().size());
    assertEquals("foo", pageRequest.getState().get("state"));
  }

  public void testRedirect() {
    PageAWithRedirect pageA = beanManager.lookupBean(PageAWithRedirect.class).getInstance();
    PageBWithRedirect pageB = beanManager.lookupBean(PageBWithRedirect.class).getInstance();
    PageCWithRedirect pageC = beanManager.lookupBean(PageCWithRedirect.class).getInstance();

    pageA.setRedirectPage(PageBWithRedirect.class);
    pageB.setRedirectPage(PageCWithRedirect.class);
    pageC.setRedirectPage(null);

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

    pageA.setRedirectPage(PageBWithRedirect.class);
    pageA.setSecondRedirectPage(PageCWithRedirect.class);
    pageB.setRedirectPage(null);
    pageC.setRedirectPage(null);

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

    pageA.setRedirectPage(PageBWithRedirect.class);
    pageB.setRedirectPage(null);

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

    pageA.setRedirectPage(PageBWithRedirect.class);
    pageB.setRedirectPage(PageCWithRedirect.class);
    pageC.setRedirectPage(PageAWithRedirect.class);

    try {
      navigation.goTo(PageAWithRedirect.class, ImmutableMultimap.<String, String>of());
      fail("Expected redirect loop exception");
    } catch (RuntimeException e) {
    }
  }

  public void testPageWithException() {
    beanManager.lookupBean(PageWithException.class).getInstance();

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
