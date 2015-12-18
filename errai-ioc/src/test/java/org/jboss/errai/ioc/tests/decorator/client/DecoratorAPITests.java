package org.jboss.errai.ioc.tests.decorator.client;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.decorator.client.res.MyDecoratedBean;
import org.jboss.errai.ioc.tests.decorator.client.res.TestDataCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class DecoratorAPITests extends AbstractErraiIOCTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.decorator.DecoratorAPITests";
  }

  public void testBeanDecoratedWithProxy() {
    final MyDecoratedBean instance = IOC.getBeanManager().lookupBean(MyDecoratedBean.class).getInstance();

    instance.someMethod("a", 1);
    instance.someMethod("b", 2);
    instance.someMethod("c", 3);

    assertEquals(instance.getTestMap(), TestDataCollector.getBeforeInvoke());
    assertEquals(instance.getTestMap(), TestDataCollector.getAfterInvoke());

    Map<String, Object> expectedProperties = new HashMap<String, Object>();
    expectedProperties.put("foobar", "foobie!");

    assertEquals(expectedProperties, TestDataCollector.getProperties());
  }

  public void testInitializationStatementsInvoked() throws Exception {
    final MyDecoratedBean instance = IOC.getBeanManager().lookupBean(MyDecoratedBean.class).getInstance();

    // setFlag(true) should be called by init callback.
    assertTrue(instance.isFlag());
  }

  public void testDestructionStatementsInvoked() throws Exception {
    final MyDecoratedBean instance = IOC.getBeanManager().lookupBean(MyDecoratedBean.class).getInstance();

    // precondition
    assertTrue(instance.isFlag());
    IOC.getBeanManager().destroyBean(instance);

    assertFalse(instance.isFlag());
  }
}
