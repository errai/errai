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

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.nav.client.local.testpages.NonCompositePage;
import org.jboss.errai.ui.nav.client.local.testpages.OtherPageWithTransitionAnchor;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithTransitionAnchor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.RootPanel;

public class TransitionAnchorTest extends AbstractErraiCDITest {

  private SyncBeanManager beanManager = null;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.nav.NavigationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    beanManager = IOC.getBeanManager();
  }

  public void testTransitionAnchorInjection() throws Exception {
    TransitionAnchorTestApp app = beanManager.lookupBean(TransitionAnchorTestApp.class).getInstance();
    assertNotNull(app);
    PageWithTransitionAnchor page = app.getPage();
    assertNotNull(page);

    // Ensure that an injected TransitionAnchor works
    assertNotNull(page.linkToB.getHref());
    assertTrue(page.linkToB.getHref().endsWith("#page_b"));

    // Now ensure that an injected TransitionAnchorFactory works
    assertEquals(5, page.getWidgetCount());
    // TransitionAnchor from factory #1
    TransitionAnchor<?> factoryAnchor = (TransitionAnchor<?>) page.getWidget(1);
    assertNotNull(factoryAnchor);
    assertTrue(factoryAnchor.getHref().endsWith("#page_b_with_state"));
    // TransitionAnchor from factory #2
    factoryAnchor = (TransitionAnchor<?>) page.getWidget(2);
    assertNotNull(factoryAnchor);
    assertTrue(factoryAnchor.getHref().endsWith("#page_b_with_state;uuid=12345"));
    // TransitionAnchor from factory #3
    factoryAnchor = (TransitionAnchor<?>) page.getWidget(3);
    assertNotNull(factoryAnchor);
    assertTrue(factoryAnchor.getHref().endsWith("#page_b_with_state;uuid=54321"));
  }

  public void testTransitionAnchorWithNonCompositePage() throws Exception {
    TransitionAnchorTestApp app = beanManager.lookupBean(TransitionAnchorTestApp.class).getInstance();
    assertNotNull(app);
    PageWithTransitionAnchor page = app.getPage();
    assertNotNull(page);

    // Ensure that an injected TransitionAnchor works
    assertNotNull(page.linkToNonComp.getHref());
    assertTrue(page.linkToNonComp.getHref().endsWith("#" + NonCompositePage.class.getSimpleName()));

    // TransitionAnchor from factory #1
    TransitionAnchor<?> factoryAnchor = page.nonCompLinkFactory.get();
    RootPanel.get().add(factoryAnchor);
    assertNotNull(factoryAnchor);
    assertTrue(factoryAnchor.getHref().endsWith("#" + NonCompositePage.class.getSimpleName()));
  }

  public void testTransitionAnchorSetDisabled() throws Exception {
    // Get nav panel
    Navigation nav = beanManager.lookupBean(Navigation.class).getInstance();
    assertNotNull(nav);
    // Get test page
    OtherPageWithTransitionAnchor page = beanManager.lookupBean(OtherPageWithTransitionAnchor.class).getInstance();
    assertNotNull(page);
    // Navigate to test page
    nav.goTo(OtherPageWithTransitionAnchor.class, (Multimap) HashMultimap.create());
    assertTrue(page.isAttached());

    // Disable anchor
    page.getAnchor().setEnabled(false);

    // Check that underlying element is disabled
    assertTrue(page.getAnchor().getElement().getPropertyBoolean("disabled"));
  }

  public void testTransitionAnchorDisabledOnClick() throws Exception {
    // Get nav panel
    Navigation nav = beanManager.lookupBean(Navigation.class).getInstance();
    assertNotNull(nav);
    // is a singleton
    // Get test page
    OtherPageWithTransitionAnchor page = beanManager.lookupBean(OtherPageWithTransitionAnchor.class).getInstance();
    assertNotNull(page);
    // Navigate to test page
    nav.goTo(OtherPageWithTransitionAnchor.class, (Multimap) HashMultimap.create());
    assertTrue(page.isAttached());

    // Make sure that click event triggers link while enabled
    ClickEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false),
            page.getAnchor(), page.getAnchor().getElement());
    assertFalse(page.isAttached());

    // Navigate back to test page
    nav.goTo(OtherPageWithTransitionAnchor.class, (Multimap) HashMultimap.create());
    assertTrue(page.isAttached());

    // Disable anchor
    page.getAnchor().setEnabled(false);
    // Fire click event
    ClickEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false),
            page.getAnchor(), page.getAnchor().getElement());

    // Check that page is still displayed
    assertTrue(page.isAttached());
  }

}
