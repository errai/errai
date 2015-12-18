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
