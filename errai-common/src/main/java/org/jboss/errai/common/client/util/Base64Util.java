package org.jboss.errai.common.client.util;


/**
 * Base64 String to-and-from byte array utility.
 * <p>
 * This code is a modified version of Base64Encoder from <a href=
 * "http://code.google.com/p/gwt-crypto/source/browse/trunk/src/main/java/com/googlecode/gwt/crypto/bouncycastle/util/encoders/Base64.java?r=50"
 * >the gwt-crypto project</a>, used under the terms of the ASL 2.0 license.
 *
 * @author gwt-crypto project (original encode/decode logic)
 * @author Jonathan Fuerth <jfuerth@gmail.com> (modified for Errai by removing
 *         usage of java.io)
 */
public class Base64Util {

  private static final byte[] encodingTable = { (byte) 'A', (byte) 'B', (byte) 'C',
      (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I',
      (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O',
      (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
      (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a',
      (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
      (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm',
      (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's',
      (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y',
      (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
      (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+',
      (byte) '/' };

  protected static final byte padding = (byte) '=';

  protected static final byte[] decodingTable = new byte[128];
  static {
    for (int i = 0; i < encodingTable.length; i++) {
      decodingTable[encodingTable[i]] = (byte) i;
    }
  }

  /**
   * Same as {@link #encode(byte[], int, int)} but accepts an array of boxed
   * Byte values.
   *
   * @param data
   *          The sequence of bytes to encode. Every element must be non-null.
   * @param off
   *          The offset into the data array to start encoding from
   * @param length
   *          The number of bytes to encode, starting from {@code off}
   * @return The base64 encoded data.
   */
  public static String encode(Byte[] data, int off, int length) {
    byte[] primitiveData = new byte[data.length];
    for (int i = 0; i < length; i++) {
      primitiveData[i] = data[off + i];
    }
    return encode(primitiveData, 0, primitiveData.length);
  }

  /**
   * Encodes the input data, producing a base 64 string.
   *
   * @param data
   *          the data to encode. It is assumed that none of the elements are
   *          null.
   * @param off
   *          The offset into the data array to start encoding from
   * @param length
   *          The number of bytes to encode, starting from {@code off}
   * @return the base64 representation of the given binary data.
   */
  public static String encode(byte[] data, int off, int length) {
    StringBuilder sb = new StringBuilder();
    int modulus = length % 3;
    int dataLength = (length - modulus);
    int a1, a2, a3;

    for (int i = off; i < off + dataLength; i += 3) {
      a1 = data[i] & 0xff;
      a2 = data[i + 1] & 0xff;
      a3 = data[i + 2] & 0xff;

      sb.append((char) encodingTable[(a1 >>> 2) & 0x3f]);
      sb.append((char) encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f]);
      sb.append((char) encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f]);
      sb.append((char) encodingTable[a3 & 0x3f]);
    }

    /*
     * process the tail end.
     */
    int b1, b2, b3;
    int d1, d2;

    switch (modulus) {
    case 0: /* nothing left to do */
      break;
    case 1:
      d1 = data[off + dataLength] & 0xff;
      b1 = (d1 >>> 2) & 0x3f;
      b2 = (d1 << 4) & 0x3f;

      sb.append((char) encodingTable[b1]);
      sb.append((char) encodingTable[b2]);
      sb.append((char) padding);
      sb.append((char) padding);
      break;
    case 2:
      d1 = data[off + dataLength] & 0xff;
      d2 = data[off + dataLength + 1] & 0xff;

      b1 = (d1 >>> 2) & 0x3f;
      b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
      b3 = (d2 << 2) & 0x3f;

      sb.append((char) encodingTable[b1]);
      sb.append((char) encodingTable[b2]);
      sb.append((char) encodingTable[b3]);
      sb.append((char) padding);
      break;
    }

    return sb.toString();
  }

  private static boolean ignore(char c) {
    return (c == '\n' || c == '\r' || c == '\t' || c == ' ');
  }

  public static Byte[] decodeAsBoxed(String data) {
    byte[] primitiveArray = decode(data);
    Byte[] boxedData = new Byte[primitiveArray.length];
    for (int i = 0; i < primitiveArray.length; i++) {
      boxedData[i] = primitiveArray[i];
    }
    return boxedData;
  }

  /**
   * Decodes the Base64-encoded String to the equivalent binary data array.
   * Whitespace characters in the input stream are ignored.
   *
   * @return the binary data decoded from the input string
   */
  public static byte[] decode(String data) {
    byte b1, b2, b3, b4;

    if (data.length() == 0) {
      return new byte[0];
    }

    int end = data.length();

    while (end > 0) {
      if (!ignore(data.charAt(end - 1))) {
        break;
      }

      end--;
    }

    byte[] lastBlock = new byte[3];
    int bytesInLastBlock = decodeLastBlock(
            lastBlock, 0,
            data.charAt(end - 4), data.charAt(end - 3),
            data.charAt(end - 2), data.charAt(end - 1));

    byte[] out = new byte[data.length() / 4 * 3 - (3 - bytesInLastBlock)];
    int outpos = 0;

    int i = 0;
    int finish = end - 4;

    i = nextI(data, i, finish);
    while (i < finish) {
      b1 = decodingTable[data.charAt(i++)];

      i = nextI(data, i, finish);

      b2 = decodingTable[data.charAt(i++)];

      i = nextI(data, i, finish);

      b3 = decodingTable[data.charAt(i++)];

      i = nextI(data, i, finish);

      b4 = decodingTable[data.charAt(i++)];

      out[outpos++] = (byte) ((b1 << 2) | (b2 >> 4));
      out[outpos++] = (byte) ((b2 << 4) | (b3 >> 2));
      out[outpos++] = (byte) ((b3 << 6) | b4);

      i = nextI(data, i, finish);
    }

    for (int j = 0; j < bytesInLastBlock; j++) {
      out[outpos++] = lastBlock[j];
    }

    return out;
  }

  private static int decodeLastBlock(
          byte[] out, int outpos, char c1, char c2, char c3, char c4) {
    byte b1, b2, b3, b4;

    if (c3 == padding) {
      b1 = decodingTable[c1];
      b2 = decodingTable[c2];

      out[outpos++] = (byte) ((b1 << 2) | (b2 >> 4));

      return 1;
    }
    else if (c4 == padding) {
      b1 = decodingTable[c1];
      b2 = decodingTable[c2];
      b3 = decodingTable[c3];

      out[outpos++] = (byte) ((b1 << 2) | (b2 >> 4));
      out[outpos++] = (byte) ((b2 << 4) | (b3 >> 2));

      return 2;
    }
    else {
      b1 = decodingTable[c1];
      b2 = decodingTable[c2];
      b3 = decodingTable[c3];
      b4 = decodingTable[c4];

      out[outpos++] = (byte) ((b1 << 2) | (b2 >> 4));
      out[outpos++] = (byte) ((b2 << 4) | (b3 >> 2));
      out[outpos++] = (byte) ((b3 << 6) | b4);

      return 3;
    }
  }

  private static int nextI(String data, int i, int finish) {
    while ((i < finish) && ignore(data.charAt(i))) {
      i++;
    }
    return i;
  }
}
