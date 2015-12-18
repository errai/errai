/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
  private final static String secureRandomAlgorithm = "SHA1PRNG";
  private final static SecureRandom random;

  static {
    try {
      random = SecureRandom.getInstance(secureRandomAlgorithm);
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
    return nextSecureHash(algorithm, seed());
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
    final byte[][] seeds;
    if (additionalSeed != null) {
      seeds = new byte[additionalSeed.length + 1][];
      System.arraycopy(additionalSeed, 0, seeds, 0, additionalSeed.length);
      seeds[seeds.length - 1] = seed();
    }
    else {
      seeds = new byte[][]{seed()};
    }

    return hashToHexString(_nextSecureHash(algorithm, seeds));
  }

  private static byte[] _nextSecureHash(final String algorithm, final byte[]... seeds) {
    try {
      final MessageDigest md = MessageDigest.getInstance(algorithm);

      for (final byte[] seed : seeds) {
        md.update(seed);
      }

      for (int i = 0; i < 100; i++) {
        md.update(md.digest());
      }

      return md.digest(new byte[64]);
    }
    catch (Exception e) {
      throw new RuntimeException("failed to generate session id hash", e);
    }
  }

  public static String hashToHexString(final byte[] hash) {
    final StringBuilder hexString = new StringBuilder(hash.length);
    for (final byte mdbyte : hash) {
      hexString.append(Integer.toHexString(0xFF & mdbyte));
    }
    return hexString.toString();
  }

  private static byte[] seed() {
    final byte[] seed = new byte[16];
    random.nextBytes(seed);
    return seed;
  }
}
