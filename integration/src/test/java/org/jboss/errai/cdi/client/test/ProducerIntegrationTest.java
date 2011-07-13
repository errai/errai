package org.jboss.errai.cdi.client.test;

import org.jboss.errai.cdi.client.ProducerTestModule;
import org.junit.Ignore;

/**
 * Tests CDI producers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Ignore
public class ProducerIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.ProducerTestModule";
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testProducers() {
    assertEquals("Failed to inject produced @A", ProducerTestModule.getInstance().getNumberA(), ProducerTestModule
            .getInstance().getTestBean().getaInteger());
    assertEquals("Failed to inject produced @B", ProducerTestModule.getInstance().getNumberB(), ProducerTestModule
            .getInstance().getTestBean().getbInteger());
    assertEquals("Failed to inject produced @C", ProducerTestModule.getInstance().getNumberC(), ProducerTestModule
            .getInstance().getTestBean().getcInteger());
    assertEquals("Failed to inject String", ProducerTestModule.getInstance().getNumberC().toString(),
            ProducerTestModule.getInstance().getTestBean().getProducedString());

  }
}