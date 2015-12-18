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

package org.jboss.errai.ui.test.extended.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Test;

import com.google.gwt.dom.client.Document;

public class ExtendedTemplateTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Test
  public void testInsertAndReplaceNestedWithCompositeTemplate() {
    ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(CompositeExtendedTemplateTestApp.class).getInstance();
    runAssertions(app);
  }

  @Test
  public void testInsertAndReplaceNestedNonCompositeTemplate() {
    ElementTemplateTestApp app = IOC.getBeanManager().lookupBean(NonCompositeExtendedTemplateTestApp.class).getInstance();
    runAssertions(app);
  }

  private void runAssertions(ElementTemplateTestApp app) {
    assertNotNull(app.getExtComponent());

    System.out.println("DUMPING: " + Document.get().getBody().getInnerHTML());

    assertNotNull(Document.get().getElementById("root"));
    assertNotNull(Document.get().getElementById("c1"));
    assertEquals("This will be rendered inside anchor", Document.get().getElementById("c1").getInnerText());
    assertNotNull(Document.get().getElementById("c2"));
    assertEquals("This will be rendered inside label c2", Document.get().getElementById("c2").getInnerText());
    assertEquals("DIV", Document.get().getElementById("c2").getTagName());
    assertNotNull(Document.get().getElementById("c3"));
    assertEquals("This will be rendered inside label c3", Document.get().getElementById("c3").getInnerText());

    assertNotNull("Field in base template should be initialized", app.getExtComponent().getC2Base());
    assertFalse("Field in base template should not be attached", app.getExtComponent().getC2Base().isAttached());

    assertNotNull("Field in extension template should be initialized", app.getExtComponent().getC2());
    assertTrue("Field in extension template should be attached", app.getExtComponent().getC2().isAttached());

    assertFalse(app.getExtComponent().getElement().getInnerHTML().contains("This will not be rendered"));

    assertNotNull(app.getSecondExtComponent());
    assertNotNull(app.getSecondExtComponent().getC2());
    assertNotNull(app.getSecondExtComponent().getContent3());
  }

}
