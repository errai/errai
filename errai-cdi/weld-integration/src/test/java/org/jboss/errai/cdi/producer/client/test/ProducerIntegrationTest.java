package org.jboss.errai.cdi.producer.client.test;


import org.jboss.errai.cdi.producer.client.ProducerDependentTestBean;
import org.jboss.errai.cdi.producer.client.ProducerTestModule;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.rebind.IOCTestRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Tests CDI producers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(IOCTestRunner.class)
public class ProducerIntegrationTest extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.producer.ProducerTestModule";
  }

  ProducerTestModule module;
  ProducerDependentTestBean testBean;

  public void setValues() {
    module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();
    testBean = IOC.getBeanManager().lookupBean(ProducerDependentTestBean.class).getInstance();
  }

  public void testInjectionUsingProducerField() {
    setValues();

    assertEquals("Failed to inject produced @A",
            module.getNumberA(),
            testBean.getIntegerA());
  }

  public void testInjectionUsingProducerMethod() {
    setValues();

    assertEquals("Failed to inject produced @B",
            module.getNumberB(),
            testBean.getIntegerB());
  }

  public void testInjectionUsingDependentProducerMethods() {
    setValues();

    assertEquals("Failed to inject produced @C",
            module.getNumberC(),
            testBean.getIntegerC());

    assertEquals("Failed to inject produced String depending on @C",
            module.getNumberC().toString(),
            testBean.getProducedString());
  }

  public void testAnyQualifiedInjection() {
    setValues();

    assertEquals("Failed to inject produced @D @E as @Any",
            module.getFloatDE(),
            testBean.getUnqualifiedFloat());
  }

  public void testSubsetQualifiedInjection() {
    setValues();

    assertEquals("Failed to inject produced @D @E as @D",
            module.getFloatDE(),
            testBean.getFloatD());
  }

  public void testCyclicalDependencyWasSatisfied() {
    setValues();

    assertEquals(testBean.getFloatD(), testBean.getFloatD());
    assertEquals(testBean.getIntegerA(), testBean.getIntegerA());
    assertEquals(testBean.getIntegerB(), testBean.getIntegerB());

    String val = "TestFieldABC";

    testBean.setTestField(val);

    assertEquals(val, testBean.getTestField());
  }
}