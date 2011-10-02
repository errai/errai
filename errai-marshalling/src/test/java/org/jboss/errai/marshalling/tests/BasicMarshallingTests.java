package org.jboss.errai.marshalling.tests;

import org.jboss.errai.marshalling.client.marshallers.ArrayMarshaller;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BasicMarshallingTests {
  @Test
  public void test1() {
     new MarshallerGeneratorFactory().generate("org.foo", "MarshallerBootstrapperImpl");
  }
  
  @Test
  public void test2() {
    Object[] array;
    
    List<List<String>> testCollection = new ArrayList<List<String>>();
    
    for (int i = 0; i < 10; i++) {
      List<String> inner = new ArrayList<String>();
      for (int i2 = 0; i2 < 5; i2++) {
        inner.add("a_" + i2);
      }
      testCollection.add(inner);
    }



    array = (Object[]) new ArrayMarshaller().demarshall(testCollection, null);


    System.out.println(array);
  }
  
}
