package org.jboss.errai.cdi.producer.client.test;


import org.jboss.errai.cdi.producer.client.ProducerDependentTestBean;
import org.jboss.errai.cdi.producer.client.ProducerTestModule;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.rebind.IOCTestRunner;
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

  public void testInjectionUsingProducerField() {
    ProducerTestModule module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();

    assertEquals("Failed to inject produced @A",
            module.getNumberA(),
            module.getTestBean().getIntegerA());
  }

  public void testInjectionUsingProducerMethod() {
    ProducerTestModule module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();

    assertEquals("Failed to inject produced @B",
            module.getNumberB(),
            module.getTestBean().getIntegerB());
  }

  public void testInjectionUsingDependentProducerMethods() {
    ProducerTestModule module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();

    assertEquals("Failed to inject produced @C",
            module.getNumberC(),
            module.getTestBean().getIntegerC());

    assertEquals("Failed to inject produced String depending on @C",
            module.getNumberC().toString(),
            module.getTestBean().getProducedString());
  }

  public void testAnyQualifiedInjection() {
    ProducerTestModule module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();

    assertEquals("Failed to inject produced @D @E as @Any",
            module.getFloatDE(),
            module.getTestBean().getUnqualifiedFloat());
  }

  public void testSubsetQualifiedInjection() {
    ProducerTestModule module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();

    assertEquals("Failed to inject produced @D @E as @D",
            module.getFloatDE(),
            module.getTestBean().getFloatD());
  }
  
  public void testCyclicalDependencyWasSatisfied() {
    ProducerDependentTestBean bean = IOC.getBeanManager().lookupBean(ProducerDependentTestBean.class).getInstance();
    ProducerTestModule module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();

    assertEquals(bean.getFloatD(), module.getTestBean().getFloatD());
    assertEquals(bean.getIntegerA(), module.getTestBean().getIntegerA());
    assertEquals(bean.getIntegerB(), module.getTestBean().getIntegerB());
    
    String val = "TestFieldABC";
    
    bean.setTestField(val);
    
    assertEquals(val, module.getTestBean().getTestField());
  }
}