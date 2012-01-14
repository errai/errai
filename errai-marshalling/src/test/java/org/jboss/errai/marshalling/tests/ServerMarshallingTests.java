package org.jboss.errai.marshalling.tests;

import junit.framework.Assert;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ServerMarshallingTests {

  static {
    System.setProperty("errai.devel.nocache", "true");
  }

  private Object testEncodeDecodeDynamic(Object value) {
    if (value == null) return null;
    return testEncodeDecode((Class<Object>) value.getClass(), value);
  }


  private <T> T testEncodeDecode(Class<T> type, T value) {
    Marshaller marshaller = MappingContextSingleton.get().getMarshaller(type.getName());
    Assert.assertNotNull("did not find " + type.getName() + " marshaller", marshaller);

    MarshallingSession encSession = MarshallingSessionProviderFactory.getEncoding();
    String enc = "[" + marshaller.marshall(value, encSession) + "]";

    MarshallingSession decSession = MarshallingSessionProviderFactory.getDecoding();
    EJValue parsedJson = ParserFactory.get().parse(enc);
    Assert.assertTrue("expected outer JSON to be array", parsedJson.isArray() != null);

    EJValue encodedNode = parsedJson.isArray().get(0);

    Object dec = marshaller.demarshall(encodedNode, decSession);
    Assert.assertTrue("decoded type not an instance of String", type.isAssignableFrom(value.getClass()));
    assertEquals(value, dec);

    return (T) dec;
  }

  private static void assertEquals(Object a1, Object a2) {
    if (a1 != null && a2 != null) {
      if (a1.getClass().isArray()) {
        if (a2.getClass().isArray()) {
          assertArrayEquals(a1, a2);
          return;
        }

      }
    }

    Assert.assertEquals(a1, a2);
  }

  private static void assertArrayEquals(Object array1, Object array2) {
    int len1 = Array.getLength(array1);
    int len2 = Array.getLength(array2);

    if (len1 != len2) Assert.failNotEquals("different length arrays!", array1, array2);

    Object el1, el2;

    for (int i = 0; i < len1; i++) {
      el1 = Array.get(array1, i);
      el2 = Array.get(array2, i);

      if ((el1 == null || el2 == null) && el1 != null) {
        Assert.failNotEquals("different values", array1, array2);
      }
      else if (el1 != null) {
        assertEquals(el1, el2);
      }
    }
  }

  @Test
  public void testString() {
    testEncodeDecode(String.class, "ThisIsOurTestString");
  }

  @Test
  public void testEscapesInString() {
    testEncodeDecode(String.class, "\n\t\r\n{}{}{}\\}\\{\\]\\[");
  }

  @Test
  public void testStringArray() {
    testEncodeDecode(String[].class, new String[]{"foo", "bar", "superfoobar", "ultrafoobar"});
  }

  @Test
  public void testIntegerMaxValue() {
    testEncodeDecode(Integer.class, Integer.MAX_VALUE);
  }

  @Test
  public void testIntegerMinValue() {
    testEncodeDecode(Integer.class, Integer.MIN_VALUE);
  }

  @Test
  public void testIntegerRandomValue() {
    testEncodeDecode(Integer.class, new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE));
  }

  @Test
  public void testShortMinValue() {
    testEncodeDecode(Short.class, Short.MAX_VALUE);
  }

  @Test
  public void testShortMaxValue() {
    testEncodeDecode(Short.class, Short.MIN_VALUE);
  }

  @Test
  public void testShortRandomValue() {
    testEncodeDecode(Short.class, (short) new Random(System.currentTimeMillis()).nextInt(Short.MAX_VALUE));
  }

  @Test
  public void testLongMaxValue() {
    testEncodeDecode(Long.class, Long.MAX_VALUE);
  }

  @Test
  public void testLongMinValue() {
    testEncodeDecode(Long.class, Long.MIN_VALUE);
  }

  @Test
  public void testLong6536000376648360988() {
    testEncodeDecode(Long.class, 6536000376648360988L);
  }

  @Test
  public void testLongRandomValue() {
    testEncodeDecode(Long.class, new Random(System.currentTimeMillis()).nextLong());
  }

  @Test
  public void testDoubleMaxValue() {
    testEncodeDecode(Double.class, Double.MAX_VALUE);
  }

  @Test
  public void testDoubleMinValue() {
    testEncodeDecode(Double.class, Double.MIN_VALUE);
  }

  @Test
  public void testDoubleRandomValue() {
    testEncodeDecode(Double.class, new Random(System.currentTimeMillis()).nextDouble());
  }

  @Test
  public void testDouble0dot9635950160419999() {
    testEncodeDecode(Double.class, 0.9635950160419999d);
  }

  @Test
  public void testFloatMaxValue() {
    testEncodeDecode(Float.class, Float.MAX_VALUE);
  }

  @Test
  public void testFloatMinValue() {
    testEncodeDecode(Float.class, Float.MIN_VALUE);
  }

  @Test
  public void testFloatRandomValue() {
    testEncodeDecode(Float.class, new Random(System.currentTimeMillis()).nextFloat());
  }

  @Test
  public void testByteMaxValue() {
    testEncodeDecode(Byte.class, Byte.MAX_VALUE);
  }

  @Test
  public void testByteMinValue() {
    testEncodeDecode(Byte.class, Byte.MIN_VALUE);
  }

  @Test
  public void testByteRandomValue() {
    testEncodeDecode(Byte.class, (byte) new Random(System.currentTimeMillis()).nextInt());
  }

  @Test
  public void testBooleanTrue() {
    testEncodeDecode(Boolean.class, Boolean.TRUE);
  }

  @Test
  public void testBooleanFalse() {
    testEncodeDecode(Boolean.class, Boolean.FALSE);
  }

  @Test
  public void testCharMaxValue() {
    testEncodeDecode(Character.class, Character.MAX_VALUE);
  }

  @Test
  public void testCharMinValue() {
    testEncodeDecode(Character.class, Character.MIN_VALUE);
  }

  @Test
  public void testListMarshall() {
    testEncodeDecodeDynamic(Arrays.asList("foo", "bar", "sillyhat"));
  }

  @Test
  public void testUnmodifiableListMarshall() {
    testEncodeDecodeDynamic(Collections.unmodifiableList(Arrays.asList("foo", "bar", "sillyhat")));
  }

  @Test
  public void testSingletonListMarshall() {
    testEncodeDecodeDynamic(Collections.singletonList("foobie"));
  }

  @Test
  public void testSetMarshall() {
    testEncodeDecodeDynamic(new HashSet(Arrays.asList("foo", "bar", "sillyhat")));
  }

  @Test
  public void testEmptyList() {
    testEncodeDecodeDynamic(Collections.emptyList());
  }

  @Test
  public void testEmptySet() {
    testEncodeDecodeDynamic(Collections.emptySet());
  }

  @Test
  public void testEmptyMap() {
    testEncodeDecodeDynamic(Collections.emptyMap());
  }

  @Test
  public void testSynchronizedSortedMap() {
    TreeMap map = new TreeMap();
    map.put("a", "a");
    map.put("b", "b");
    map.put("c", "c");
    testEncodeDecodeDynamic(Collections.synchronizedSortedMap(map));
  }

  @Test
  public void testSynchronizedMap() {
    HashMap map = new HashMap();
    map.put("a", "a");
    map.put("b", "b");
    map.put("c", "c");
    testEncodeDecodeDynamic(Collections.synchronizedMap(map));
  }
}
