package org.jboss.errai.cdi.invalid.producer.client.test;

import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.MockIOCGenerator;
import org.junit.Test;

/**
 * Tests CDI producers.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvalidProducerIntegrationTest {

  @Test
  public void testInvalidProducers() throws InstantiationException, IllegalAccessException {
    try {
      MockIOCGenerator mockIOCGenerator = new MockIOCGenerator();
      mockIOCGenerator.setPackageFilter("org.jboss.errai.cdi.invalid.producer");
      mockIOCGenerator.generate().newInstance().bootstrapContainer();
      fail("expected a nice exception explaining which injection points could not be satisfied and why");
    } catch (Exception e) {
      //TODO we want detailed exception information here
      //expected
    }
  }
}