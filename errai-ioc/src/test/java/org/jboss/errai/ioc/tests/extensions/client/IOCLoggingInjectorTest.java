package org.jboss.errai.ioc.tests.extensions.client;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.extensions.client.res.ClassWithLoggerField;
import org.jboss.errai.ioc.tests.extensions.client.res.ClassWithNamedLoggerField;

public class IOCLoggingInjectorTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.extensions.IOCExtensionTests";
  }

  public void testSimpleLoggerFieldInjection() throws Exception {
    ClassWithLoggerField instance = IOC.getBeanManager().lookupBean(ClassWithLoggerField.class).getInstance();
    assertNotNull("Logger was not injected", instance.getLogger());
    assertEquals("Logger should have name of enclosing class", ClassWithLoggerField.class.getName(), instance
            .getLogger().getName());
  }

  public void testNamedLoggerFieldInjection() throws Exception {
    ClassWithNamedLoggerField instance = IOC.getBeanManager().lookupBean(ClassWithNamedLoggerField.class).getInstance();
    assertNotNull("Logger was not injected", instance.getLogger());
    assertEquals("Logger should have had the given name", ClassWithNamedLoggerField.LOGGER_NAME, instance.getLogger()
            .getName());
  }

}
