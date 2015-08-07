package org.jboss.errai.ioc.tests.wiring.client;

import org.jboss.errai.ioc.client.WindowInjectionContext;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.JsTypeProvider;
import org.jboss.errai.ioc.client.container.Proxy;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeConsumer;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeDependentBean;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeDependentInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeSingletonBean;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeSingletonInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.JsTypeUnimplemented;

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
    final JsTypeUnimplemented ref = new JsTypeUnimplemented() {
    };

    final WindowInjectionContext wndContext = WindowInjectionContext.createOrGet();
    wndContext.addBeanProvider("fake", new JsTypeProvider<JsTypeUnimplemented>() {
      @Override
      public JsTypeUnimplemented getInstance() {
        return ref;
      }
    });
    wndContext.addSuperTypeAlias(JsTypeUnimplemented.class.getName(), "fake");

    final IOCBeanDef<JsTypeConsumer> consumer = IOC.getBeanManager().lookupBean(JsTypeConsumer.class);

    assertSame(ref, ((Proxy<JsTypeUnimplemented>) consumer.getInstance().getIface()).unwrappedInstance());
  }
}
