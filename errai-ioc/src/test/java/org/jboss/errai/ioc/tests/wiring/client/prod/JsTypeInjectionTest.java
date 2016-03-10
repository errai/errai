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

package org.jboss.errai.ioc.tests.wiring.client.prod;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.ioc.client.WindowInjectionContext;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.JsTypeProvider;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.res.AlternativeImpl;
import org.jboss.errai.ioc.tests.wiring.client.res.ConsumesProducedJsType;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeConsumer;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeDependentBean;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeDependentInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeSingletonBean;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeSingletonInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.MultipleImplementationsJsType;
import org.jboss.errai.ioc.tests.wiring.client.res.NativeConcreteJsType;
import org.jboss.errai.ioc.tests.wiring.client.res.NativeTypeTestModule;
import org.jboss.errai.ioc.tests.wiring.client.res.ProducedJsType;
import org.jboss.errai.ioc.tests.wiring.client.res.UnimplementedType;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Timer;

public class JsTypeInjectionTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    WindowInjectionContext.reset();
    final WindowInjectionContext windowInjContext = WindowInjectionContext.createOrGet();
    windowInjContext.addBeanProvider(MultipleImplementationsJsType.class.getName(), new JsTypeProvider<Object>() {
      @Override
      public Object getInstance() {
        return new AlternativeImpl(1);
      }
    });
    windowInjContext.addBeanProvider(MultipleImplementationsJsType.class.getName(), new JsTypeProvider<Object>() {
      @Override
      public Object getInstance() {
        return new AlternativeImpl(2);
      }
    });
    super.gwtSetUp();
  }

  public void testSingletonJsTypeInWindowContext() {
    final WindowInjectionContext wndContext = WindowInjectionContext.createOrGet();

    final Object bean1 = wndContext.getBean(JsTypeSingletonBean.class.getName());
    assertNotNull("@JsType bean was not registered in window context", bean1);

    final Object bean2 = wndContext.getBean(JsTypeSingletonInterface.class.getName());
    assertNotNull("@JsType bean was not registered using its interface", bean2);

    assertSame(bean1, bean2);
  }

  public void testDependentJsTypeInWindowContext() {
    final WindowInjectionContext wndContext = WindowInjectionContext.createOrGet();

    final Object bean1 = wndContext.getBean(JsTypeDependentBean.class.getName());
    assertNotNull("@JsType bean was not registered in window context", bean1);

    final Object bean2 = wndContext.getBean(JsTypeDependentInterface.class.getName());
    assertNotNull("@JsType bean was not registered using its interface", bean2);

    assertNotSame(bean1, bean2);
  }

  public void testConsumingOfUnimplementedJsType() throws Exception {
    injectScriptThenRun(() -> {
      final UnimplementedType ref = new UnimplementedType() {

        @Override
        public void overloaded(Object obj) {
        }

        @Override
        protected void overloaded() {
        }
      };

      final WindowInjectionContext wndContext = WindowInjectionContext.createOrGet();
      wndContext.addBeanProvider("org.jboss.errai.ioc.tests.wiring.client.res.UnimplementedType", () -> ref);

      final SyncBeanDef<JsTypeConsumer> consumer = IOC.getBeanManager().lookupBean(JsTypeConsumer.class);

      assertSame(ref, Factory.maybeUnwrapProxy(consumer.getInstance().getIface()));
    });
  }

  private void injectScriptThenRun(final Runnable test) {
    final String scriptUrl = getScriptUrl();
    final Timer timeoutFail = new Timer() {

      @Override
      public void run() {
        fail("Timed out waiting to load " + scriptUrl);
      }
    };

    timeoutFail.schedule(9000);
    delayTestFinish(10000);
    ScriptInjector.fromUrl(scriptUrl)
                  .setWindow(ScriptInjector.TOP_WINDOW)
                  .setCallback(new Callback<Void, Exception>() {

                    @Override
                    public void onFailure(Exception reason) {
                      timeoutFail.cancel();
                      fail("Could not load " + scriptUrl);
                    }

                    @Override
                    public void onSuccess(Void result) {
                      try {
                        test.run();
                        finishTest();
                      } finally {
                        timeoutFail.cancel();
                      }
                    }
                  }).inject();
  }

  private String getScriptUrl() {
    final String scriptUrl = GWT.getModuleBaseForStaticFiles() + "native.js";
    return scriptUrl;
  }

  public void testNativeJsTypesNotInWindowContext() throws Exception {
    injectScriptThenRun(() -> {
      final WindowInjectionContext context = WindowInjectionContext.createOrGet();
      try {
        context.getBean("org.jboss.errai.ioc.tests.wiring.client.res.NativeConcreteJsType");
        fail("There should not be a provider in the WindowInjectionContext for NativeConcreteJsType.");
      } catch (IOCResolutionException ex) {
      }
    });
  }

  public void testInstantiableNativeJsTypeIsInjectable() throws Exception {
    injectScriptThenRun(() -> {
      try {
        final NativeTypeTestModule module = IOC.getBeanManager().lookupBean(NativeTypeTestModule.class).getInstance();
        final NativeConcreteJsType instance = module.nativeConcreteJsType;
        assertEquals("Not the expected implementation (in native.js).", "I am a native type!", instance.message());
      } catch (IOCResolutionException ex) {
        fail("Precondition failed: Problem looking up test module.");
      }
    });
  }

  public void testProducerMethodNativeOfJsType() throws Exception {
    injectScriptThenRun(() -> {
      try {
        final NativeTypeTestModule module = IOC.getBeanManager().lookupBean(NativeTypeTestModule.class).getInstance();
        assertNotNull(module.producedNativeIface);
        assertEquals("Not the expected implementation (in native.js).", "please", module.producedNativeIface.getMagicWord());
      } catch (IOCResolutionException ex) {
        fail("Precondition failed: Problem looking up test module.");
      }
    });
  }

  public void testProducerMethodOfJsType() throws Exception {
    final ConsumesProducedJsType consumer = IOC.getBeanManager().lookupBean(ConsumesProducedJsType.class).getInstance();
    assertNotNull(consumer.instance);
    assertTrue(consumer.instance instanceof ProducedJsType);
    assertEquals(1, IOC.getBeanManager().lookupBeans(ProducedJsType.class).size());
  }

  public void testMultipleJsTypeImplementations() throws Exception {
    Collection<SyncBeanDef<MultipleImplementationsJsType>> beans = IOC.getBeanManager().lookupBeans(MultipleImplementationsJsType.class);
    assertEquals(2, beans.size());

    final Set<Integer> values = new HashSet<>();
    for (final SyncBeanDef<MultipleImplementationsJsType> bean : beans) {
      final MultipleImplementationsJsType instance = bean.getInstance();
      assertTrue(instance instanceof AlternativeImpl);
      values.add(instance.value());
    }

    assertEquals(new HashSet<>(Arrays.asList(1, 2)), values);

    WindowInjectionContext.createOrGet().addBeanProvider(MultipleImplementationsJsType.class.getName(), new JsTypeProvider<Object>() {
      @Override
      public Object getInstance() {
        return new AlternativeImpl(3);
      }
    });
    values.clear();
    beans = IOC.getBeanManager().lookupBeans(MultipleImplementationsJsType.class);
    for (final SyncBeanDef<MultipleImplementationsJsType> bean : beans) {
      values.add(bean.getInstance().value());
    }
    assertEquals(new HashSet<>(Arrays.asList(1, 2, 3)), values);
  }
}
