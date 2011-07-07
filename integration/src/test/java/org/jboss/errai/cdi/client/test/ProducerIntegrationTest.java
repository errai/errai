package org.jboss.errai.cdi.client.test;

import org.jboss.errai.cdi.client.ProducerTestModule;
import org.junit.Ignore;

/**
 * Tests CDI producers.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Ignore public class ProducerIntegrationTest extends AbstractErraiCDITest {

  @Override public String getModuleName() {
    return "org.jboss.errai.cdi.ProducerTestModule";
  }

  @Override public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testProducers() {
    assertNotNull("Failed to produce @A", ProducerTestModule.getInstance().getNumberA());
    assertNotNull("Failed to produce @B", ProducerTestModule.getInstance().getNumberB());
    assertNotNull("Failed to produce @C", ProducerTestModule.getInstance().getNumberC());

    assertEquals("Failed to inject produced @A",
                ProducerTestModule.getInstance().getNumberA(),
                ProducerTestModule.getInstance().getInjectedNumberA());
    assertEquals("Failed to inject produced @B",
                ProducerTestModule.getInstance().getNumberB(),
                ProducerTestModule.getInstance().getInjectedNumberB());
    assertEquals("Failed to inject produced @C",
                new Integer(ProducerTestModule.getInstance().getNumberC()).toString(),
                ProducerTestModule.getInstance().getInjectedString());
  }
}