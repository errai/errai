/**
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

package org.jboss.errai.ui.test.quickhandler.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.junit.Test;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;

public class QuickHandlerTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceWithCompositeTemplate() {
    final QuickHandlerTemplateTestApp app = IOC.getBeanManager().lookupBean(CompositeQuickHandlerTemplateTestApp.class).getInstance();
    runAssertions(app);
    IOC.getBeanManager().destroyBean(app);
  }

  @Test
  public void testInsertAndReplaceWithNonCompositeTemplate() {
    final QuickHandlerTemplateTestApp app = IOC.getBeanManager().lookupBean(NonCompositeQuickHandlerTemplateTestApp.class).getInstance();
    runAssertions(app);
    IOC.getBeanManager().destroyBean(app);
  }

  private void runAssertions(QuickHandlerTemplateTestApp app) {
    assertNotNull(app.getComponent());

    assertFalse(app.getComponent().isThisEventFired());
    DomEvent.fireNativeEvent(generateClickEvent(), app.getComponent());
    assertTrue(app.getComponent().isThisEventFired());

    DivElement c0 = DivElement.as(Document.get().getElementById("c0"));
    assertNotNull(c0);
    AnchorElement c1 = app.getComponent().getC1();
    ButtonElement c2 = ButtonElement.as(Document.get().getElementById("c2"));
    ButtonElement c3 = ButtonElement.as(TemplateUtil.asElement(app.getComponent().getC3()));
    assertNotNull(c2);

    assertFalse(app.getComponent().isC0EventFired());
    assertFalse(app.getComponent().isC0EventFired2());
    c0.dispatchEvent(generateClickEvent());
    assertTrue(app.getComponent().isC0EventFired());
    assertFalse(app.getComponent().isC0EventFired2());

    assertFalse(app.getComponent().isC1_dupEventFired());
    assertFalse(app.getComponent().isC1EventFired());
    c1.dispatchEvent(generateClickEvent());
    assertTrue(app.getComponent().isC1EventFired());
    assertTrue(app.getComponent().isC1_dupEventFired());

    assertFalse(app.getComponent().isC2EventFired());
    c2.click();
    assertTrue(app.getComponent().isC2EventFired());

    assertFalse(app.getComponent().isC3EventFired());
    c3.dispatchEvent(generateClickEvent());
    assertTrue(app.getComponent().isC3EventFired());
  }

  private NativeEvent generateClickEvent() {
    return Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false);
  }

}