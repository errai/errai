package org.jboss.errai.cdi.invalid.producer.client.test;

import org.jboss.errai.cdi.client.AbstractErraiCDITest;
import org.junit.Test;

/**
 * Tests CDI producers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvalidProducerIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.invalid.producer.InvalidProducerTestModule";
  }

  @Test
  public void testInvalidProducers() {
    try {
      super.gwtSetUp();
      fail("expected a nice exception explaining which injection points could not be satisfied and why!");
    } catch (Exception e) {
      //TODO we want detailed exception information here
      //expected
    }
  }
}