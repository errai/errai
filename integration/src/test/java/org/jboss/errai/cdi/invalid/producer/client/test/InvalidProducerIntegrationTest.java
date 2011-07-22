package org.jboss.errai.cdi.invalid.producer.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.MockIOCGenerator;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependency;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedField;
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
      fail("Expected an UnsatisfiedDependenciesException");
    } 
    catch (UnsatisfiedDependenciesException e) {
      assertEquals("Expected to find excatly 1 unsatisfied dependency", 1, e.getUnsatisfiedDependecies().size());
      UnsatisfiedDependency dependency = e.getUnsatisfiedDependecies().get(0);
      assertTrue("Should be an unsatisfied field", dependency instanceof UnsatisfiedField);
      assertEquals("Wrong field name", ((UnsatisfiedField) dependency).getField().getName(), "abInteger");
      assertEquals("Wrong enclosing type", dependency.getEnclosingType().getName(), "InvalidProducerDependentTestBean");
      assertEquals("Wrong injected type", dependency.getInjectedType().getName(), "Integer");
    }
  }
}