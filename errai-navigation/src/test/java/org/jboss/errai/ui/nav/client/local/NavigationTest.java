package org.jboss.errai.ui.nav.client.local;

import java.util.Collection;

import junit.framework.AssertionFailedError;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.nav.client.local.testpages.CircularRef1;
import org.jboss.errai.ui.nav.client.local.testpages.CircularRef2;
import org.jboss.errai.ui.nav.client.local.testpages.PageIsWidget;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithExtraState;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithLinkToIsWidget;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithRole;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class NavigationTest extends AbstractErraiCDITest {

  private Navigation navigation;
  private NavigationGraph navGraph;

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
  }

  public void testMissingPage() throws Exception {
    try {
      navGraph.getPage("page that does not exist");
      fail("Did not get an exception for a missing page");
    } catch (IllegalArgumentException ex) {
      assertTrue(ex.getMessage().contains("page that does not exist"));
    }
  }

  public void testPageWithDefaultName() throws Exception {
    PageNode<?> pageA = navGraph.getPage("PageA");
    assertNotNull(pageA);
    assertEquals("PageA", pageA.name());
  }

  public void testPageWithProvidedName() throws Exception {
    PageNode<?> pageB = navGraph.getPage("page_b");
    assertNotNull(pageB);
    assertEquals("page_b", pageB.name());
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
      assertTrue(pageNode.name() + " is not a page annotated with the admin role", pageNode.name().matches("Page.?WithRole"));
    }
  }

  public void testGetPageWithDefaultRole() {
    final PageNode pageByRole = navGraph.getPageByRole(DefaultPage.class);
    assertNotNull(pageByRole);
    assertEquals("PageA", pageByRole.name());
  }

  public void testIsWidgetPage() {
    navigation.goTo(PageIsWidget.class, ImmutableMultimap.<String, String>of());
  }

  public void testTransitionToIsWidgetPage() {
    PageWithLinkToIsWidget page = IOC.getBeanManager().lookupBean(PageWithLinkToIsWidget.class).getInstance();
    TransitionTo<PageIsWidget> transitionToIsWidget = page.getTransitionToIsWidget();
    assertNotNull(transitionToIsWidget);
    transitionToIsWidget.go();
    assertEquals(PageIsWidget.class, navigation.currentPage.contentType());
    assertNotNull(page.getLinkToIsWidget());
  }

  public void testLinkToIsWidgetPage() {
    PageWithLinkToIsWidget page = IOC.getBeanManager().lookupBean(PageWithLinkToIsWidget.class).getInstance();
    TransitionAnchor<PageIsWidget> anchorToIsWidget = page.getLinkToIsWidget();
    assertNotNull(anchorToIsWidget);
    anchorToIsWidget.click();
    assertEquals(PageIsWidget.class, navigation.currentPage.contentType());
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
    }, 30000, 500);
  }
  
  public void testAddNavToRootPanel() {
    runPostAttachTests(new Runnable() {

      @Override
      public void run() {
        RootPanel.get().add(navigation.getContentPanel());
        assertEquals("Navigation Panel should still be attached to the RootPanel", RootPanel.get(), navigation
                .getContentPanel().asWidget().getParent());
      }
    }, 30000, 500);
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
    }, 30000, 500);
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
   * Give the bootstrapper time to attach the Navigation content panel to the RootPanel and then run
   * a test.
   * 
   * @param test
   *          The test code to be executed after the content panel is attached
   * @param timeout
   *          The time in milliseconds to wait for the bootstrapper before giving up
   * @param interval
   *          The interval in milliseconds at which to poll the content panel while waiting for
   *          bootstrapping
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

}
