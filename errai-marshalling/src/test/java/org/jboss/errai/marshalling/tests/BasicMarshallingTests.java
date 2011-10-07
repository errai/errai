package org.jboss.errai.marshalling.tests;

import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BasicMarshallingTests {
  @Test
  public void test1() {
     new MarshallerGeneratorFactory().generate("org.foo", "MarshallerBootstrapperImpl");
  }
}
