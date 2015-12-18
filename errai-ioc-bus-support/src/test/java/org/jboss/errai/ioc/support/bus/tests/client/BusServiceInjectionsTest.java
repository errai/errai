package org.jboss.errai.ioc.support.bus.tests.client;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.support.bus.tests.client.res.SimpleBean;

/**
 * @author Mike Brock
 */
public class BusServiceInjectionsTest extends AbstractErraiIOCBusTest {

  public void testBusGetsInjected() {
    SimpleBean simpleBean = IOC.getBeanManager().lookupBean(SimpleBean.class).getInstance();
    assertNotNull(simpleBean);

    final MessageBus expected = ErraiBus.get();
    assertEquals(expected, simpleBean.getBus());
    assertEquals(expected, simpleBean.getBus2());
    assertEquals(expected, simpleBean.getBus3());
    assertEquals(expected, simpleBean.getBus4());
    assertEquals(expected, simpleBean.getClientMessageBus());

    assertNotNull(simpleBean.getDispatcher());
    assertNotNull(simpleBean.getDispatcher2());
    assertNotNull(simpleBean.getDispatcher3());
    assertNotNull(simpleBean.getDispatcher4());
  }
}
