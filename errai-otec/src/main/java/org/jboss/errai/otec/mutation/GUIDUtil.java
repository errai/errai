package org.jboss.errai.otec.mutation;

/**
 * Simple GUID utility that implements a modified form of the Linear Congruent Generator (LCG) algorithm,
 * to create psuedo-random GUIDs. The generator is double seeded by system time. Once when the GUIDUtil class
 * is loaded, and then its seeded again based on the time that createGUID is called.
 *
 * @author Mike Brock
 */
public final class GUIDUtil {
  private GUIDUtil() {
  }

  private static final char[] chars =
      {'a', 'b', 'c', 'd', 'e', 'f', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

  private static volatile int seedCounter = -((int) System.currentTimeMillis() >> 10);

  public synchronized static String createGUID() {
    int x = 938203, y = 138301, z = Integer.MAX_VALUE;
    double[] arr = new double[1000];
    long time = System.currentTimeMillis();
    seedCounter += time >> 8;

    arr[0] = System.currentTimeMillis() + seedCounter;

    for (int i = 1; i < arr.length; i++) {
      arr[i] = (x * arr[i - 1] + y) % z;
      x += time << 16 % z;
      y += time << 10 % z;
    }

    final char[] charArray = new char[35];
    for (int i = 1; i < charArray.length; i++) {
      if (i != (charArray.length - 1) && i % 5 == 0) {
        charArray[i] = ':';
        continue;
      }

      int i1 = (x + ++seedCounter) % arr.length;

      if (i1 < 0) {
        i1 = -i1;
      }

      double rand = arr[i1];

      if (rand < 0) {
        rand = -rand;
      }

      charArray[i] = chars[(int) rand % chars.length];
    }

    return new String(charArray, 1, charArray.length - 1);
  }
}
