package org.jboss.errai.bus.server.io.websockets.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author : Michel Werren
 */
public class KeystoreFactory {

  /**
   * Creates and initialize a key store.
   * 
   * @param keyStorePath
   * @param keyStorePassword
   * @param keystoreType
   * @return
   */
  public static KeyStore getKeyStore(final String keyStorePath,
          final String keyStorePassword, final String keystoreType) {
    try {
      final KeyStore ks = KeyStore.getInstance(keystoreType);
      final FileInputStream ksIS = new FileInputStream(keyStorePath);
      ks.load(ksIS, keyStorePassword.toCharArray());
      return ks;
    } catch (FileNotFoundException e) {
      throw new RuntimeException("could not find key store for path: "
              + keyStorePath, e);
    } catch (CertificateException e) {
      throw new RuntimeException("could not load keystore", e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("could not load keystore", e);
    } catch (IOException e) {
      throw new RuntimeException("could not load keystore", e);
    } catch (KeyStoreException e) {
      throw new RuntimeException("could not load keystore", e);
    }
  }
}
