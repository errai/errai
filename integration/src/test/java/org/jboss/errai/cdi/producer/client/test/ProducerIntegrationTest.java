package org.jboss.errai.cdi.producer.client.test;


import org.jboss.errai.cdi.producer.client.ProducerTestModule;
import org.jboss.errai.ioc.client.IOCClientTestCase;
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


  public void testProducers() {
    assertEquals("Failed to inject produced @A", 
        ProducerTestModule.getInstance().getNumberA(),
        ProducerTestModule.getInstance().getTestBean().getIntegerA());
    
    assertEquals("Failed to inject produced @B", 
        ProducerTestModule.getInstance().getNumberB(), 
        ProducerTestModule.getInstance().getTestBean().getIntegerB());
    
    assertEquals("Failed to inject produced @C", 
        ProducerTestModule.getInstance().getNumberC(), 
        ProducerTestModule.getInstance().getTestBean().getIntegerC());

    assertEquals("Failed to inject produced String depending on @C", 
        ProducerTestModule.getInstance().getNumberC().toString(),
        ProducerTestModule.getInstance().getTestBean().getProducedString());

    assertEquals("Failed to inject produced @D unqualified", 
        ProducerTestModule.getInstance().getFloatD(), 
        ProducerTestModule.getInstance().getTestBean().getUnqualifiedFloat());
  }  
}