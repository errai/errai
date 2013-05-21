package org.jboss.errai.ui.nav.client.local;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.user.client.Window;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ui.nav.client.local.spi.NavigationGraph;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;
import org.jboss.errai.ui.nav.client.local.testpages.CircularRef1;
import org.jboss.errai.ui.nav.client.local.testpages.CircularRef2;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithExtraState;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithRole;

import java.util.Collection;

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

  public void testLoginPage() throws Exception {
    PageNode<?> loginPage = navGraph.getPage("loginPage");
    assertNotNull(loginPage);
    assertEquals("PageThatIsTheLoginPage", loginPage.name());
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
}
