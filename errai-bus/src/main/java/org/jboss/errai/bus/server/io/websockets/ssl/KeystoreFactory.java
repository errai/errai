/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.io.websockets.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;

/**
 * @author Michel Werren
 */
public class KeystoreFactory {

  /**
   * Creates and initialize a key store.
   * 
   * @param keyStorePath
   * @param keyStorePassword
   * @param keystoreType
   * @return the key store, never null.
   */
  public static KeyStore getKeyStore(final String keyStorePath, final String keyStorePassword, final String keystoreType) {
    try {
      final KeyStore ks = KeyStore.getInstance(keystoreType);
      final FileInputStream ksIS = new FileInputStream(keyStorePath);
      ks.load(ksIS, keyStorePassword.toCharArray());
      return ks;
    } 
    catch (FileNotFoundException e) {
      throw new RuntimeException("could not find key store for path: " + keyStorePath, e);
    } 
    catch (Exception e) {
      throw new RuntimeException("could not load keystore", e);
    }
  }
}
