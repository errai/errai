package org.jboss.errai.bus.client.tests;

import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;

/**
 * @author Mike Brock
 */
public class GeneratorTestSimple {
  public static void main(String[] args) {
    String s = new MarshallerGeneratorFactory().generate("org.jboss.errai.marshalling.client.api", "MarshallerFactoryImpl");
    System.out.println(s);
  }
}
