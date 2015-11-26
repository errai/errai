package org.jboss.errai.ioc.tests.wiring.client.prod;

import org.jboss.errai.ioc.client.WindowInjectionContext;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.JsTypeProvider;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeConsumer;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeDependentBean;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeDependentInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeSingletonBean;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeSingletonInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.NativeFactory;
import org.jboss.errai.ioc.tests.wiring.client.res.NativeType;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Timer;

public class JsTypeInjectionTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
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
    final String scriptUrl = GWT.getModuleBaseForStaticFiles() + "native.js";
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
                    public void onSuccess(Void result) {
                      final NativeType ref = NativeFactory.get();

                      final WindowInjectionContext wndContext = WindowInjectionContext.createOrGet();
                      wndContext.addBeanProvider("org.jboss.errai.ioc.tests.wiring.client.res.NativeType", new JsTypeProvider<NativeType>() {
                        @Override
                        public NativeType getInstance() {
                          return ref;
                        }
                      });

                      final SyncBeanDef<JsTypeConsumer> consumer = IOC.getBeanManager().lookupBean(JsTypeConsumer.class);

                      assertSame(ref, Factory.maybeUnwrapProxy(consumer.getInstance().getIface()));
                      timeoutFail.cancel();
                      finishTest();
                    }

                    @Override
                    public void onFailure(Exception reason) {
                      timeoutFail.cancel();
                      fail("Could not load " + scriptUrl);
                    }
                  }).inject();
  }
}
