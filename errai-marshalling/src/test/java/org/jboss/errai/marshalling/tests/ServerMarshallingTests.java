package org.jboss.errai.marshalling.tests;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOuputTarget;
import org.jboss.errai.marshalling.server.EncodingSession;
import org.jboss.errai.marshalling.server.JSONStreamDecoder;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;
import org.junit.Test;

import java.util.Random;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ServerMarshallingTests {

  static {
    System.setProperty("errai.devel.nocache", "true");
  }


  private <T> T testEncodeDecocde(Class<T> type, T value) {
    Marshaller marshaller = MappingContextSingleton.get().getMarshaller(type.getName());
    Assert.assertNotNull("did not find " + String.class.getName() + " marshaller", marshaller);

    MarshallingSession encSession = MarshallingSessionProviderFactory.getEncoding();
    String enc = "[" + marshaller.marshall(value, encSession) + "]";

    MarshallingSession decSession = MarshallingSessionProviderFactory.getDecoding();
    EJValue parsedJson = ParserFactory.get().parse(enc);
    Assert.assertTrue("expected outer JSON to be array", parsedJson.isArray() != null);

    EJValue encodedNode = parsedJson.isArray().get(0);

    Object dec = marshaller.demarshall(encodedNode, decSession);
    Assert.assertTrue("decoded type not an instance of String", type.isAssignableFrom(value.getClass()));
    Assert.assertEquals(value, dec);

    return (T) dec;
  }

  @Test
  public void testString() {
    testEncodeDecocde(String.class, "ThisIsOurTestString");
  }

  @Test
  public void testIntegerMaxValue() {
    testEncodeDecocde(Integer.class, Integer.MAX_VALUE);
  }

  @Test
  public void testIntegerMinValue() {
    testEncodeDecocde(Integer.class, Integer.MIN_VALUE);
  }

  @Test
  public void testIntegerRandomValue() {
    testEncodeDecocde(Integer.class, new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE));
  }

  @Test
  public void testShortMinValue() {
    testEncodeDecocde(Short.class, Short.MAX_VALUE);
  }

  @Test
  public void testShortMaxValue() {
    testEncodeDecocde(Short.class, Short.MIN_VALUE);
  }

  @Test
  public void testShortRandomValue() {
    testEncodeDecocde(Short.class, (short) new Random(System.currentTimeMillis()).nextInt(Short.MAX_VALUE));
  }

  @Test
  public void testLongMaxValue() {
    testEncodeDecocde(Long.class, Long.MAX_VALUE);
  }

  @Test
  public void testLongMinValue() {
    testEncodeDecocde(Long.class, Long.MIN_VALUE);
  }

  @Test
  public void testLong6536000376648360988() {
    testEncodeDecocde(Long.class, 6536000376648360988L);
  }

  @Test
  public void testLongRandomValue() {
    testEncodeDecocde(Long.class, new Random(System.currentTimeMillis()).nextLong());
  }

  @Test
  public void testDoubleMaxValue() {
    testEncodeDecocde(Double.class, Double.MAX_VALUE);
  }

  @Test
  public void testDoubleMinValue() {
    testEncodeDecocde(Double.class, Double.MIN_VALUE);
  }

  @Test
  public void testDoubleRandomValue() {
    testEncodeDecocde(Double.class, new Random(System.currentTimeMillis()).nextDouble());
  }

  @Test
  public void testFloatMaxValue() {
    testEncodeDecocde(Float.class, Float.MAX_VALUE);
  }

  @Test
  public void testFloatMinValue() {
    testEncodeDecocde(Float.class, Float.MIN_VALUE);
  }

  @Test
  public void testFloatRandomValue() {
    testEncodeDecocde(Float.class, new Random(System.currentTimeMillis()).nextFloat());
  }

}
