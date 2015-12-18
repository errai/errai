package org.jboss.errai.common.client.util;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Tests for the base64 codec. Most of the testing is to ensure that the padding
 * logic works properly, since there are special cases for 0, 1, or 2 characters
 * of padding in the encoded string.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Base64Tests {

  public void encodeDecodeArray(int length) throws Exception {
    byte[] empty = new byte[length];
    for (int i = 0; i < length; i++) {
      empty[i] = (byte) (i + 1);
    }
    String encoded = Base64Util.encode(empty, 0, length);
    byte[] decoded = Base64Util.decode(encoded);
    assertArrayEquals(empty, decoded);
  }

  @Test
  public void testEncodeEmptyArray() throws Exception {
    encodeDecodeArray(0);
  }

  @Test
  public void testEncodeArrayLength1() throws Exception {
    encodeDecodeArray(1);
  }

  @Test
  public void testEncodeArrayLength2() throws Exception {
    encodeDecodeArray(2);
  }

  @Test
  public void testEncodeArrayLength3() throws Exception {
    encodeDecodeArray(3);
  }

  @Test
  public void testEncodeArrayLength20() throws Exception {
    encodeDecodeArray(20);
  }

  @Test
  public void testEncodeArrayLength21() throws Exception {
    encodeDecodeArray(21);
  }

  @Test
  public void testEncodeArrayLength22() throws Exception {
    encodeDecodeArray(22);
  }

  @Test
  public void testEncodeArrayLength23() throws Exception {
    encodeDecodeArray(23);
  }

  @Test
  public void testEncodeArrayLength24() throws Exception {
    encodeDecodeArray(24);
  }

}
