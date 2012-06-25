package org.jboss.errai.cdi.invalid.producer.client.test;

import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependency;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedField;
import org.jboss.errai.ioc.rebind.ioc.test.harness.MockIOCGenerator;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests scenarios in which no valid producer can be found to satisfy an injection point.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvalidProducerIntegrationTest {

  /**
   * In this case, a producer is available for @A but the injection point specifies @A @B
   *
   * @throws IllegalAccessException -
   * @throws InstantiationException -
   */
  @Test
  public void testInvalidProducerWithMissingQualifier() throws InstantiationException, IllegalAccessException {
    try {
      final Set<String> packages = new HashSet<String>();
      packages.add("org.jboss.errai.cdi.invalid.producer");
      packages.add("org.jboss.errai.cdi.invalid.producer.client");

      final MockIOCGenerator mockIOCGenerator = new MockIOCGenerator(packages);
      mockIOCGenerator.generate().newInstance().bootstrapContainer();
      fail("Expected an UnsatisfiedDependenciesException");
    }
    catch (UnsatisfiedDependenciesException e) {
      assertEquals("Expected to find excatly 1 unsatisfied dependency", 1, e.getUnsatisfiedDependencies().size());

      final UnsatisfiedDependency dependency = e.getUnsatisfiedDependencies().get(0);
      assertTrue("Should be an unsatisfied field", dependency instanceof UnsatisfiedField);
      assertEquals("Wrong field name", ((UnsatisfiedField) dependency).getField().getName(), "abInteger");
      assertEquals("Wrong enclosing type", dependency.getEnclosingType().getName(), "InvalidProducerDependentTestBean");
      assertEquals("Wrong injected type", dependency.getInjectedType().getName(), "Integer");
    }
    //todo: the mockboostrapper needs to provide a case for this
    catch (ExceptionInInitializerError e) {

    }
  }
}