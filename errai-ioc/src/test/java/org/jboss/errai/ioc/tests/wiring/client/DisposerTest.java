package org.jboss.errai.ioc.tests.wiring.client;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.res.BeanWithDisposer;

/**
 * @author Mike Brock
 */
public class DisposerTest extends AbstractErraiIOCTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  public void testInjectedDisposerWorks() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        BeanWithDisposer bean = IOC.getBeanManager().lookupBean(BeanWithDisposer.class).getInstance();

        assertNotNull(bean);
        assertNotNull(bean.getDependentBeanDisposer());

        bean.dispose();

        assertFalse("bean should have been disposed", IOC.getBeanManager().isManaged(bean.getBean()));
        assertFalse("outer bean should have been disposed", IOC.getBeanManager().isManaged(bean));

        assertTrue("bean's destructor should have been called", bean.getBean().isPreDestroyCalled());

        finishTest();
      }
    });
  }
}
