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
 * @author Mike Brock
 */
public class SecureHashUtil {
  final static String secureRandomAlgorithm = "SHA1PRNG";
  final static SecureRandom random;

  static {
    try {
      random = SecureRandom.getInstance(secureRandomAlgorithm);
      random.setSeed(SecureRandom.getInstance(secureRandomAlgorithm).generateSeed(64));
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("runtime does not support secure random algorithm: " + secureRandomAlgorithm);
    }
  }

  public static String nextSecureHash(final String algorithm) {
    return nextSecureHash(algorithm, null);
  }

  public static String nextSecureHash(final String algorithm, final String additionalSeed) {
    try {
      final MessageDigest md = MessageDigest.getInstance(algorithm);

      md.update(String.valueOf(System.nanoTime()).getBytes());

      if (additionalSeed != null) {
        md.update(additionalSeed.getBytes());
      }

      byte[] randBytes = new byte[64];
      random.nextBytes(randBytes);

      md.update(randBytes);

      return hashToHexString(md.digest());
    }
    catch (Exception e) {
      throw new RuntimeException("failed to generate session id hash", e);
    }
  }

  public static String hashToHexString(byte[] hash) {
    final StringBuilder hexString = new StringBuilder();
    for (byte mdbyte : hash) {
      hexString.append(Integer.toHexString(0xFF & mdbyte));
    }
    return hexString.toString();
  }


}
