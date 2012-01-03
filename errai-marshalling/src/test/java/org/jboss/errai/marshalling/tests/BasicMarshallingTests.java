package org.jboss.errai.marshalling.tests;

import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOuputTarget;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BasicMarshallingTests {
  @Test
  public void test1() {
    for (int i = 0; i < 2; i++) {
      String output = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.GWT)
              .generate("org.foo", "MarshallerBootstrapperImpl" );
      System.out.println(output);
    }
  }

  @Test
  public void test2() {
    System.out.println(Object[].class.getName());
  }
}
