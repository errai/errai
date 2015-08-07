/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.test.stylebinding.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;
import org.jboss.errai.ui.test.stylebinding.client.res.StyleBoundTemplate;
import org.jboss.errai.ui.test.stylebinding.client.res.StyleControl;
import org.jboss.errai.ui.test.stylebinding.client.res.TestModel;

/**
 * @author Mike Brock
 */
public class StyleBindingTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ui.test.stylebinding.Test";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    setRemoteCommunicationEnabled(false);
    super.gwtSetUp();
    StyleBindingsRegistry.get().updateStyles();
  }

  public void testStyleBinding() {
    final IOCBeanDef<StyleBoundTemplate> bean = IOC.getBeanManager().lookupBean(StyleBoundTemplate.class);
    final StyleBoundTemplate instance = bean.getInstance();

    assertEquals("hidden", instance.getTestA().getElement().getStyle().getVisibility());

    final IOCBeanDef<StyleControl> styleControl = IOC.getBeanManager().lookupBean(StyleControl.class);
    styleControl.getInstance().setAdmin(true);

    StyleBindingsRegistry.get().updateStyles();

    assertEquals("visible", instance.getTestA().getElement().getStyle().getVisibility());
  }

  public void testDataBindingChangesUpdatesStyle() {
    final IOCBeanDef<StyleBoundTemplate> bean = IOC.getBeanManager().lookupBean(StyleBoundTemplate.class);
    final StyleBoundTemplate instance = bean.getInstance();

    assertEquals("", instance.getTestB().getElement().getStyle().getVisibility());

    instance.getTestModel().setTestB("0");

    assertEquals("0", instance.getTestB().getText());
    assertEquals("hidden", instance.getTestB().getElement().getStyle().getVisibility());
  }

  public void testCustomComponentDataBindingChangesUpdatesStyle() {
    final IOCBeanDef<StyleBoundTemplate> bean = IOC.getBeanManager().lookupBean(StyleBoundTemplate.class);
    final StyleBoundTemplate instance = bean.getInstance();

    assertEquals("", instance.getTestC().getElement().getStyle().getVisibility());

    instance.getTestModel().setTestC("0");

    assertEquals("0", instance.getTestC().getValue());
    assertEquals("hidden", instance.getTestC().getElement().getStyle().getVisibility());
  }

  public void testDestroyingBeanCleansUpStyleBindings() {
    final IOCBeanDef<StyleBoundTemplate> bean = IOC.getBeanManager().lookupBean(StyleBoundTemplate.class);
    final StyleBoundTemplate instance = bean.getInstance();

    assertEquals("hidden", instance.getTestA().getElement().getStyle().getVisibility());

    IOC.getBeanManager().destroyBean(instance);

    final IOCBeanDef<StyleControl> styleControl = IOC.getBeanManager().lookupBean(StyleControl.class);
    styleControl.getInstance().setAdmin(true);

    StyleBindingsRegistry.get().updateStyles();

    assertEquals("hidden", instance.getTestA().getElement().getStyle().getVisibility());
  }

  public void testDestroyingBeanCleansUpPropertyChangeHandler() {
    final StyleBindingsRegistry oldReg = StyleBindingsRegistry.get();
    try {
      StyleBindingsRegistry registry = new StyleBindingsRegistry() {
        @Override
        public void updateStyles(Object beanInst) {
          fail("updateStyles should not be called after bean was destroyed");
        }
      };

      final IOCBeanDef<StyleBoundTemplate> bean = IOC.getBeanManager().lookupBean(StyleBoundTemplate.class);
      final StyleBoundTemplate instance = bean.getInstance();
      TestModel model = instance.getTestModel();
      model.setTestB("");

      assertEquals("", instance.getTestB().getText());
      assertEquals("", instance.getTestB().getElement().getStyle().getVisibility());

      IOC.getBeanManager().destroyBean(instance);
      StyleBindingsRegistry.set(registry);

      model.setTestB("0");
      assertEquals("", instance.getTestB().getText());
      assertEquals("", instance.getTestB().getElement().getStyle().getVisibility());
    }
    finally {
      StyleBindingsRegistry.set(oldReg);
    }
  }

}
