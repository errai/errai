package org.jboss.errai.ioc.support.bus.tests;

import org.jboss.errai.bus.client.tests.AbstractErraiTest;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;

/**
 * @author Mike Brock
 */
public abstract class AbstractErraiIOCBusTest extends AbstractErraiTest {
  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    new IOCBeanManagerLifecycle().resetBeanManager();
    new Container().bootstrapContainer();
  }
}
