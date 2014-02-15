package org.jboss.errai.ioc.async.tests.extensions.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.tests.extensions.client.res.ClassWithLoggerField;
import org.jboss.errai.ioc.async.tests.extensions.client.res.ClassWithNamedLoggerField;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;

public class IOCLoggingInjectorTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.tests.extensions.IOCExtensionTests";
  }

  public void testSimpleLoggerFieldInjection() throws Exception {
    delayTestFinish(10000);
    IOC.getAsyncBeanManager().lookupBean(ClassWithLoggerField.class)
            .getInstance(new CreationalCallback<ClassWithLoggerField>() {

              @Override
              public void callback(ClassWithLoggerField instance) {
                assertNotNull("Logger was not injected", instance.getLogger());
                assertEquals("Logger should have name of enclosing class", ClassWithLoggerField.class.getName(),
                        instance.getLogger().getName());
                finishTest();
              }
            });
  }

  public void testNamedLoggerFieldInjection() throws Exception {
    delayTestFinish(10000);
    IOC.getAsyncBeanManager().lookupBean(ClassWithNamedLoggerField.class)
            .getInstance(new CreationalCallback<ClassWithNamedLoggerField>() {

              @Override
              public void callback(ClassWithNamedLoggerField instance) {
                assertNotNull("Logger was not injected", instance.getLogger());
                assertEquals("Logger should have had the given name", ClassWithNamedLoggerField.LOGGER_NAME, instance
                        .getLogger().getName());
                finishTest();
              }
            });
  }

}
