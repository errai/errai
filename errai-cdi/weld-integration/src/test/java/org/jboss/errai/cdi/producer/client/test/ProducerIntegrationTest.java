package org.jboss.errai.cdi.producer.client.test;


import org.jboss.errai.cdi.producer.client.BeanConstrConsumesOwnProducer;
import org.jboss.errai.cdi.producer.client.BeanConsumesOwnProducer;
import org.jboss.errai.cdi.producer.client.ProducerDependentTestBean;
import org.jboss.errai.cdi.producer.client.ProducerDependentTestBeanWithCycle;
import org.jboss.errai.cdi.producer.client.ProducerTestModule;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * Tests CDI producers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ProducerIntegrationTest extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.producer.ProducerTestModule";
  }

  ProducerTestModule module;
  ProducerDependentTestBean testBean;

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
    module = IOC.getBeanManager().lookupBean(ProducerTestModule.class).getInstance();
    testBean = IOC.getBeanManager().lookupBean(ProducerDependentTestBean.class).getInstance();
  }

  public void testInjectionUsingProducerField() {
    assertEquals("Failed to inject produced @A",
            module.getNumberA(),
            testBean.getIntegerA());
  }

  public void testInjectionUsingProducerMethod() {
    assertEquals("Failed to inject produced @B",
            module.getNumberB(),
            testBean.getIntegerB());
  }

  public void testInjectionUsingDependentProducerMethods() {
    assertEquals("Failed to inject produced @C",
            module.getNumberC(),
            testBean.getIntegerC());

    assertEquals("Failed to inject produced String depending on @C",
            module.getNumberC().toString(),
            testBean.getProducedString());
  }

  public void testAnyQualifiedInjection() {
    assertEquals("Failed to inject produced @D @E as @Any",
            module.getFloatDE(),
            testBean.getUnqualifiedFloat());
  }

  public void testSubsetQualifiedInjection() {
    assertEquals("Failed to inject produced @D @E as @D",
            module.getFloatDE(),
            testBean.getFloatD());
  }

  public void testCyclicalDependencyWasSatisfied() {
    assertEquals(testBean.getFloatD(), testBean.getFloatD());
    assertEquals(testBean.getIntegerA(), testBean.getIntegerA());
    assertEquals(testBean.getIntegerB(), testBean.getIntegerB());

    String val = "TestFieldABC";
    testBean.setTestField(val);
    assertEquals(val, testBean.getTestField());
  }

  public void testStaticProducers() {
    assertNotNull("bean was not injected!", testBean.getStaticallyProducedBean());
    assertNotNull("bean was not injected!", testBean.getStaticallyProducedBeanB());
  }

  public void testCycleThroughAProducedInterface() {
    ProducerDependentTestBeanWithCycle bean = IOC.getBeanManager()
            .lookupBean(ProducerDependentTestBeanWithCycle.class).getInstance();

    assertNotNull(bean);
    assertNotNull(bean.getFooface());
    assertEquals("HiThere", bean.getFooface().getMessage());
  }

  public void testBeanCanConsumeProducerFromItself() {
    BeanConsumesOwnProducer bean = IOC.getBeanManager().lookupBean(BeanConsumesOwnProducer.class).getInstance();

    assertNotNull(bean);
    assertNotNull("bean did not inject its own producer", bean.getMagic());
  }

  public void testBeanCanConsumerProducerFromItselfThroughConstrCycle() {
    BeanConstrConsumesOwnProducer bean = IOC.getBeanManager().lookupBean(BeanConstrConsumesOwnProducer.class).getInstance();

    assertNotNull(bean);
    assertNotNull(bean.getThing());
  }
}