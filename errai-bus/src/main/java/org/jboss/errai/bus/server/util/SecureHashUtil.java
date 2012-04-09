/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A utility class for producing secure hashes throughout the Errai code where needed.
 *
 * @author Mike Brock
 */
public class SecureHashUtil {
  final static String secureRandomAlgorithm = "SHA1PRNG";
  final static SecureRandom random;

  final static long[] saltTable;

  static {
    try {
      random = SecureRandom.getInstance(secureRandomAlgorithm);
      random.setSeed(SecureRandom.getInstance(secureRandomAlgorithm).generateSeed(64));

      // build the salting table
      SecureRandom sec1 = SecureRandom.getInstance(secureRandomAlgorithm);
      sec1.setSeed(SecureRandom.getSeed(64));

      SecureRandom sec2 = SecureRandom.getInstance(secureRandomAlgorithm);
      sec1.setSeed(SecureRandom.getSeed(64));

      SecureRandom sec3 = SecureRandom.getInstance(secureRandomAlgorithm);
      sec1.setSeed(SecureRandom.getSeed(64));

      SecureRandom sec4 = SecureRandom.getInstance(secureRandomAlgorithm);
      sec1.setSeed(SecureRandom.getSeed(64));


      saltTable = new long[random.nextInt(500) + 500];

      for (int i = 0; i < saltTable.length; i++) {
        switch (random.nextInt(Integer.MAX_VALUE) % 4) {
          case 0:
            saltTable[i] = sec1.nextLong();
            break;
          case 1:
            saltTable[i] = sec2.nextLong();
            break;
          case 2:
            saltTable[i] = sec3.nextLong();
            break;
          case 3:
            saltTable[i] = sec4.nextLong();
            break;
        }
      }
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("runtime does not support secure random algorithm: " + secureRandomAlgorithm);
    }
  }

  /**
   * Returns a new secure hash using the SHA-256 hash function salted with the SHA1PRNG random number generator.
   *
   * @return a hex string representation of the hash.
   */
  public static String nextSecureHash() {
    return nextSecureHash("SHA-256");
  }

  /**
   * Returns a new secure hash using the specified hash function salted with the SHA1PRNG random number generator.
   *
   * @param algorithm The hash function to use (SHA-1, SHA-256, MD5).
   * @return a hex string representation of the hash.
   */
  public static String nextSecureHash(final String algorithm) {
    return nextSecureHash(algorithm, SecureRandom.getSeed(128));
  }

  /**
   * Get a new secure hash. Optionally accepts additional seeds which will be used to salt the hash function. A
   * secure random number generator (SHA1PRNG) is used as a base salt, compounded with a time-based hash seed.
   *
   * @param algorithm      The hash function to use (SHA-1, SHA-256, MD5).
   * @param additionalSeed A vararg of additional byte[] seeds to optionally add additional salts to the hash function.
   * @return a hex string representation of the hash.
   */
  public static String nextSecureHash(final String algorithm, final byte[]... additionalSeed) {
    byte[][] seeds;
    if (additionalSeed != null) {
      seeds = new byte[additionalSeed.length + 2][];
      System.arraycopy(additionalSeed, 0, seeds, 0, additionalSeed.length);
      seeds[seeds.length - 2] = simpleSeed(getSalt());
      seeds[seeds.length - 1] = simpleSeed(getSalt());
    }
    else {
      seeds = new byte[][]{simpleSeed(getSalt()), simpleSeed(getSalt())};
    }

    return hashToHexString(_nextSecureHash(algorithm, seeds));
  }

  private static byte[] _nextSecureHash(final String algorithm, final byte[]... additionalSeed) {
    try {
      final MessageDigest md = MessageDigest.getInstance(algorithm);

      if (additionalSeed != null) {
        for (byte[] seed : additionalSeed) {
          md.update(seed);
        }
      }

      byte[] randBytes = new byte[64];
      random.nextBytes(randBytes);
      md.update(randBytes);

      for (int i = 0; i < 1000; i++) {
        md.update(md.digest());
      }

      return md.digest(new byte[64]);
    }
    catch (Exception e) {
      throw new RuntimeException("failed to generate session id hash", e);
    }
  }

  public static String hashToHexString(byte[] hash) {
    final StringBuilder hexString = new StringBuilder(hash.length);
    for (byte mdbyte : hash) {
      hexString.append(Integer.toHexString(0xFF & mdbyte));
    }
    return hexString.toString();
  }

  private static byte[] simpleSeed(long salt) {
    return _nextSecureHash("SHA-256",
            String.valueOf(System.nanoTime() % Long.MAX_VALUE).getBytes(), String.valueOf(salt).getBytes());
  }

  // doesn't need to be synchronized. merely increments to decrease chances of nanoTime collisions.
  private static long saltCounter = random.nextLong();
  private static long getSalt() {
    int index = (int) ((System.nanoTime() + (++saltCounter)) % saltTable.length);
    if (index < 0) {
      index = -index;
    }

    return saltTable[index];
  }

  public static void main(String[] args) {
    System.out.println(nextSecureHash("SHA-256"));
  }
}
