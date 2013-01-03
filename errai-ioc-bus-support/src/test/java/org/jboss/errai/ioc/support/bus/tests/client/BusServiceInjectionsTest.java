package org.jboss.errai.ioc.support.bus.tests.client;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.support.bus.tests.client.res.SimpleBean;

/**
 * @author Mike Brock
 */
public class BusServiceInjectionsTest extends AbstractErraiIOCBusTest {

  public void testBusGetsInjected() {
    SimpleBean simpleBean = IOC.getBeanManager().lookupBean(SimpleBean.class).getInstance();
    assertNotNull(simpleBean);

    assertEquals(ErraiBus.get(), simpleBean.getBus());
    assertEquals(ErraiBus.get(), simpleBean.getBus2());
    assertEquals(ErraiBus.get(), simpleBean.getBus3());
    assertEquals(ErraiBus.get(), simpleBean.getBus4());

    assertNotNull(simpleBean.getDispatcher());
    assertNotNull(simpleBean.getDispatcher2());
    assertNotNull(simpleBean.getDispatcher3());
    assertNotNull(simpleBean.getDispatcher4());
  }
}
