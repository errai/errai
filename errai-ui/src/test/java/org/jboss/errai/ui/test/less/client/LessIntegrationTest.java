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

package org.jboss.errai.ui.test.less.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.local.spi.LessStyle;
import org.jboss.errai.ui.client.local.spi.LessStyleMapping;
import org.jboss.errai.ui.test.less.client.res.PageTemplate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author edewit@redhat.com
 */
public class LessIntegrationTest extends AbstractErraiCDITest {

  private LessStyle lessStyle;
  private PageTemplate template;

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    GWT.create(LessStyleMapping.class);
    super.gwtSetUp();
    lessStyle = IOC.getBeanManager().lookupBean(LessStyle.class).getInstance();
    template = IOC.getBeanManager().lookupBean(PageTemplate.class).getInstance();
  }

  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  public void testIntegrateLessFileInHead() {
    assertNotNull(template.getBox());
    final String expected = "Eoxl56A";
    assertEquals(expected, lessStyle.get("box"));
    assertEquals(expected, template.getBox().getClassName());
  }

  public void testStyleRuleOrderPreserved() throws Exception {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        RootPanel.get().add(template);
        assertEquals("The width from simple-override.less should have been used for this element.", 200, template
                .getBox().getClientWidth());
        finishTest();
      }
    });
  }
}
