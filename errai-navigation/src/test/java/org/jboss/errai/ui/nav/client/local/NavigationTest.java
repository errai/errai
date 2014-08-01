package org.jboss.errai.ui.nav.client.local;

import java.util.Collection;

import junit.framework.AssertionFailedError;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.PageNotFoundException;
import org.jboss.errai.ui.nav.client.local.pushstate.PushStateUtil;
import org.jboss.errai.ui.nav.client.local.res.TestNavigationErrorHandler;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.nav.client.local.testpages.CircularRef1;
import org.jboss.errai.ui.nav.client.local.testpages.CircularRef2;
import org.jboss.errai.ui.nav.client.local.testpages.MissingPageRole;
import org.jboss.errai.ui.nav.client.local.testpages.MissingUniquePageRole;
import org.jboss.errai.ui.nav.client.local.testpages.PageA;
import org.jboss.errai.ui.nav.client.local.testpages.PageIsWidget;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithExtraState;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithLinkToIsWidget;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithNavigationControl;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithRole;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class NavigationTest extends AbstractErraiCDITest {

  private static final int TIMEOUT = 30000;
  private Navigation navigation;
  private NavigationGraph navGraph;
  private HandlerRegistration historyHandlerRegistration;
  private int historyHandlerInvocations;
  private HistoryTokenFactory htFactory;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    navigation = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
    navGraph = navigation.getNavGraph();
    htFactory = IOC.getBeanManager().lookupBean(HistoryTokenFactory.class).getInstance();
    History.newItem("", false);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    navigation.cleanUp();
    super.gwtTearDown();
  }

  public void testMissingPage() throws Exception {
    try {
      navGraph.getPage("page that does not exist");
      fail("Did not get an exception for a missing page");
    } catch (PageNotFoundException ex) {
      assertTrue(ex.getMessage().contains("page that does not exist"));
    }
  }

  public void testDefaultErrorHandlerWithMissingPage() throws Exception {
    navigation.goTo("page_b"); // navigate to a known non-default page before
                               // test because the expected behavior is that it
                               // navigates back to the default page

    navigation.goTo("page that does not exist");
    assertEquals("did not navigate to default page", PageA.class, navigation.currentPage.contentType());

  }

  public void testErrorHandlerWithMissingPageThroughGoTo() throws Exception {
    TestNavigationErrorHandler newNavigationHandler = new TestNavigationErrorHandler();
    navigation.setErrorHandler(newNavigationHandler);

    navigation.goTo("page that does not exist");
    assertEquals("did not enter new handler", 1, newNavigationHandler.count);
  }

  public void testErrorHandlerWithMissingPageThroughHistoryNewItem() throws Exception {
    TestNavigationErrorHandler newNavigationHandler = new TestNavigationErrorHandler();
    navigation.setErrorHandler(newNavigationHandler);

    History.newItem("page that does not exist");
    assertEquals("did not enter new handler", 1, newNavigationHandler.count);
  }

  public void testMissingPageRole() throws Exception {
    navigation.goTo("page_b"); // navigate to a known non-default page before
    // test because the expected behavior is that it
    // navigates back to the default page

    navigation.goToWithRole(MissingPageRole.class);
    assertEquals("did not navigate to default page", PageA.class, navigation.currentPage.contentType());
  }

  public void testMissingPageRoleWithTestErrorHandler() throws Exception {
    TestNavigationErrorHandler newNavigationHandler = new TestNavigationErrorHandler();
    navigation.setErrorHandler(newNavigationHandler);

    navigation.goToWithRole(MissingPageRole.class);
    assertEquals("Did not go through test error handler", 1, newNavigationHandler.count);
  }

  public void testPageWithDefaultName() throws Exception {
    PageNode<?> pageA = navGraph.getPage("PageA");
    assertNotNull(pageA);
    assertEquals("PageA", pageA.name());
  }

  public void testPageWithProvidedName() throws Exception {
    PageNode<?> pageB = navGraph.getPage("PageB");
    assertNotNull(pageB);
    assertEquals("PageB", pageB.name());
  }

  public void testCircularReferences() throws Exception {
    PageNode<CircularRef1> cr1Node = navGraph.getPage(CircularRef1.class);

    // now fetch the bean instance
    final CircularRef1[] workaround = new CircularRef1[1];
    cr1Node.produceContent(new CreationalCallback<CircularRef1>() {
      @Override
      public void callback(CircularRef1 beanInstance) {
        workaround[0] = beanInstance;
      }
    });

    assertNotNull("CreationalCallback should have been invoked before produceContent returned!", workaround[0]);

    CircularRef1 cr1 = workaround[0];
    TransitionTo<CircularRef2> transitionToCR2 = cr1.getLink();
    Class<CircularRef2> cr2Type = transitionToCR2.toPageType();
    assertEquals(CircularRef2.class, cr2Type);
  }

  public void testUrlUpdatesWithPageChange() throws Exception {
    navigation.goTo(PageWithExtraState.class, ImmutableMultimap.of("intThing", "42"));
    assertEquals("#PageWithExtraState;intThing=42", Window.Location.getHash());
  }

  public void testUrlUpdateWithPageChangeByRole() {
    navigation.goToWithRole(DefaultPage.class);
    assertEquals("#PageA", Window.Location.getHash());
  }

  public void testGetPageByRole() throws Exception {
    final Collection<PageNode<?>> pageByRole = navGraph.getPagesByRole(PageWithRole.AdminPage.class);
    assertNotNull(pageByRole);
    assertFalse(pageByRole.isEmpty());

    assertTrue(pageByRole.size() == 2);
    for (PageNode<?> pageNode : pageByRole) {
      assertTrue(pageNode.name() + " is not a page annotated with the admin role",
              pageNode.name().matches("Page.?WithRole"));
    }
  }

  public void testGetMissingPageByRole() throws Exception {
    final Collection<PageNode<? extends IsWidget>> pagesByRole = navGraph.getPagesByRole(MissingPageRole.class);

    assertNotNull(pagesByRole);
    assertTrue(pagesByRole.isEmpty());
  }

  public void testGetMissingPageByUniqueRole() throws Exception {
    Throwable thrown = null;
    try {
      navGraph.getPageByRole(MissingUniquePageRole.class);
    } catch (Throwable e) {
      thrown = e;
    }

    assertNotNull("Expected an exception to be thrown.", thrown);
    assertEquals(MissingPageRoleException.class, thrown.getClass());
  }

  public void testGetPageWithDefaultRole() {
    final PageNode<?> pageByRole = navGraph.getPageByRole(DefaultPage.class);
    assertNotNull(pageByRole);
    assertEquals("PageA", pageByRole.name());
  }

  public void testIsWidgetPage() {
    final PageIsWidget page = IOC.getBeanManager().lookupBean(PageIsWidget.class).getInstance();

    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        historyHandlerRegistration.removeHandler();
        assertEquals(PageIsWidget.class, navigation.getCurrentPage().contentType());
        assertEquals(page.asWidget(), ((SimplePanel) navigation.getContentPanel().asWidget()).getWidget());
        finishTest();
      }
    });

    delayTestFinish(5000);
    navigation.goTo(PageIsWidget.class, ImmutableMultimap.<String, String> of());
  }

  public void testIsWidgetPageTransition() {
    final PageWithLinkToIsWidget page = IOC.getBeanManager().lookupBean(PageWithLinkToIsWidget.class).getInstance();
    final PageIsWidget targetPage = IOC.getBeanManager().lookupBean(PageIsWidget.class).getInstance();

    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        historyHandlerRegistration.removeHandler();
        assertEquals(PageIsWidget.class, navigation.getCurrentPage().contentType());
        assertEquals(targetPage.asWidget(), ((SimplePanel) navigation.getContentPanel().asWidget()).getWidget());
        finishTest();
      }
    });

    delayTestFinish(5000);
    page.getTransitionToIsWidget().go();
  }

  public void testIsWidgetAnchorTransition() {
    final PageWithLinkToIsWidget page = IOC.getBeanManager().lookupBean(PageWithLinkToIsWidget.class).getInstance();
    final PageIsWidget targetPage = IOC.getBeanManager().lookupBean(PageIsWidget.class).getInstance();

    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        historyHandlerRegistration.removeHandler();
        assertEquals(PageIsWidget.class, navigation.getCurrentPage().contentType());
        assertEquals(targetPage.asWidget(), ((SimplePanel) navigation.getContentPanel().asWidget()).getWidget());
        finishTest();
      }
    });

    delayTestFinish(5000);
    page.getLinkToIsWidget().click();
  }

  public void testNotAttachedToRootPanel() {
    assertNull("Navigation Content Panel should not yet have a parent", navigation.getContentPanel().asWidget()
            .getParent());
  }

  public void testAutomaticRootPanelAttachment() {
    runPostAttachTests(new Runnable() {
      @Override
      public void run() {
        assertEquals("Navigation Panel was not automatically attached", RootPanel.get(), navigation.getContentPanel()
                .asWidget().getParent());
      }
    }, TIMEOUT, 500);
  }

  public void testAddNavToRootPanel() {
    runPostAttachTests(new Runnable() {

      @Override
      public void run() {
        RootPanel.get().add(navigation.getContentPanel());
        assertEquals("Navigation Panel should still be attached to the RootPanel", RootPanel.get(), navigation
                .getContentPanel().asWidget().getParent());
      }
    }, TIMEOUT, 500);
  }

  public void testAddNavPanelToOtherPanel() {
    runPostAttachTests(new Runnable() {

      @Override
      public void run() {
        SimplePanel newParent = new SimplePanel();
        newParent.add(navigation.getContentPanel());

        assertEquals("Navigation panel should be child of newParent", newParent, navigation.getContentPanel()
                .asWidget().getParent());
        assertEquals("Navigation panel shoudl not be attached to the RootPanel", -1,
                RootPanel.get().getWidgetIndex(navigation.getContentPanel()));
      }
    }, TIMEOUT, 500);
  }

  public void testManuallyAttachToRootPanelBefore() {
    assertNull("Navigation content panel should not yet be attached", navigation.getContentPanel().asWidget()
            .getParent());
    RootPanel.get().add(navigation.getContentPanel());

    // Force navigation to run Navigation#maybeAttachContentPanel()
    navigation.goToWithRole(DefaultPage.class);

    assertEquals("RootPanel should still be parent", RootPanel.get(), navigation.getContentPanel().asWidget()
            .getParent());

  }

  public void testManuallyAttachToOtherPanelBefore() {
    assertNull("Navigation content panel should not yet be attached", navigation.getContentPanel().asWidget()
            .getParent());
    SimplePanel panel = new SimplePanel();
    panel.add(navigation.getContentPanel());

    // Force navigation to run Navigation#maybeAttachContentPanel()
    navigation.goToWithRole(DefaultPage.class);

    assertEquals(panel, navigation.getContentPanel().asWidget().getParent());
  }

  /**
   * Test if a history event is fired when navigation is done.
   */
  public void testHistoryEventFired() {
    final PageWithLinkToIsWidget page = IOC.getBeanManager().lookupBean(PageWithLinkToIsWidget.class).getInstance();

    historyHandlerInvocations = 0;
    historyHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        historyHandlerInvocations++;
        // the first invocation is triggered by the init vote
        if (historyHandlerInvocations == 1) {
          page.getLinkToIsWidget().click();
        }
        else {
          historyHandlerRegistration.removeHandler();
          finishTest();
        }
      }
    });

    delayTestFinish(5000);
  }

  public void testNavigationControl() throws Exception {
    final PageWithNavigationControl page = IOC.getBeanManager().lookupBean(PageWithNavigationControl.class)
            .getInstance();

    navigation.goTo(PageWithNavigationControl.class, ArrayListMultimap.<String, String> create());
    assertEquals(PageWithNavigationControl.class, navigation.getCurrentPage().contentType());

    navigation.goTo(PageA.class, ArrayListMultimap.<String, String> create());
    assertEquals(PageWithNavigationControl.class, navigation.getCurrentPage().contentType());

    page.control.proceed();
    assertEquals(PageA.class, navigation.getCurrentPage().contentType());
  }

  /**
   * Give the bootstrapper time to attach the Navigation content panel to the RootPanel and then run a test.
   * 
   * @param test
   *          The test code to be executed after the content panel is attached
   * @param timeout
   *          The time in milliseconds to wait for the bootstrapper before giving up
   * @param interval
   *          The interval in milliseconds at which to poll the content panel while waiting for bootstrapping
   */
  private void runPostAttachTests(final Runnable test, final int timeout, final int interval) {
    delayTestFinish(timeout + 2 * interval);
    final long startTime = System.currentTimeMillis();

    new Timer() {

      @Override
      public void run() {
        if (System.currentTimeMillis() - startTime >= timeout) {
          this.cancel();
        }
        else {
          try {
            assertEquals(RootPanel.get(), navigation.getContentPanel().asWidget().getParent());

          } catch (AssertionFailedError e) {
            return;
          }
          test.run();
          this.cancel();
          finishTest();
        }

      }
    }.scheduleRepeating(interval);
  }
  
  public void testURLWithExtraKeyValuePairs() throws Exception {
    String url = "page/123/string;var3=4";
    HistoryToken encodedToken = htFactory.parseURL(url);
    assertEquals("Unexpected state map contents: " + encodedToken.getState(), "123", encodedToken.getState()
            .get("var1").iterator().next());
    assertEquals("Unexpected state map contents: " + encodedToken.getState(), "string",
            encodedToken.getState().get("var2").iterator().next());
    assertEquals("Unexpected state map contents: " + encodedToken.getState(), "4", encodedToken.getState().get("var3")
            .iterator().next());
  }

  public void testPageStateWithOneExtraParam() throws Exception {
    String pageName = "PageWithPathParameters";
    Builder<String, String> builder = ImmutableMultimap.builder();
    builder.put("var1", "123");
    builder.put("var2", "string");
    builder.put("var3", "4");
    
    Multimap<String, String> pageStateMap = builder.build();
    String decodedToken = URL.decodePathSegment(htFactory.createHistoryToken(pageName, pageStateMap).toString());
    assertEquals("Incorrect HistoryToken URL generated: " + decodedToken, "page/123/string;var3=4", decodedToken);
  }
  
  public void testPageStateWithMultipleExtraParams() throws Exception {
    String pageName = "PageWithPathParameters";
    Builder<String, String> builder = ImmutableMultimap.builder();
    builder.put("var1", "123");
    builder.put("var2", "string");
    builder.put("var3", "4");
    builder.put("var4", "thing");
    
    Multimap<String, String> pageStateMap = builder.build();
    String decodedToken = URL.decodePathSegment(htFactory.createHistoryToken(pageName, pageStateMap).toString());
    assertEquals("Incorrect HistoryToken URL generated: " + decodedToken, "page/123/string;var3=4&var4=thing", decodedToken);
  }
  
  public void testForEmptyContextWithoutPushState() throws Exception {
    runPostAttachTests(new Runnable() {

      @Override
      public void run() {
        assertFalse(PushStateUtil.isPushStateActivated());
        assertEquals("", Navigation.getAppContext());
      }
      
    }, TIMEOUT, 500);
  }
}
