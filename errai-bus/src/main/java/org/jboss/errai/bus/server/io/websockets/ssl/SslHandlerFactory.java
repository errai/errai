package org.jboss.errai.bus.server.io.websockets.ssl;

import io.netty.handler.ssl.SslHandler;
import org.apache.commons.lang.StringUtils;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * Factory for the {@link io.netty.handler.ssl.SslHandler} to provide wss scheme.
 *
 * @author : Michel Werren
 */
public class SslHandlerFactory {

  private static KeyStore keyStore = null;

  private static SSLEngine sslEngine = null;

  private static String keyPassword = null;

  private static final Logger LOGGER = LoggerFactory
          .getLogger(SslHandlerFactory.class.getName());

  /**
   * Build a {@link io.netty.handler.ssl.SslHandler} when the side band server
   * is configured to use ssl for websocket.
   * 
   * @param esc
   * @return
   */
  public static SslHandler buildSslHandler(ErraiServiceConfigurator esc) {
    if (ErraiConfigAttribs.SECURE_WEB_SOCKET_SERVER.getBoolean(esc)) {
      keyPassword = StringUtils.isEmpty(keyPassword) ? ErraiConfigAttribs.WEB_SOCKET_KEY_PASSWORD
              .get(esc) : keyPassword;
      // Init key store only once
      if (keyStore == null) {
        final String keyStorePath = ErraiConfigAttribs.WEB_SOCKET_KEYSTORE
                .get(esc);
        final String keystoreType = ErraiConfigAttribs.WEB_SOCKET_KEYSTORE_TYPE
                .get(esc);

        if (StringUtils.isEmpty(keyStorePath)) {
          throw new IllegalStateException(
                  "when ssl is activated for the sideband server, key store information are necessary");
        }
        String keyStorePassword = ErraiConfigAttribs.WEB_SOCKET_KEYSTORE_PASSWORD
                .get(esc);

        if (StringUtils.isEmpty(keyStorePassword)) {
          throw new IllegalStateException(
                  "keystore configured for sideband websocket server, but missing keystore password");
        }

        if (StringUtils.isEmpty(keyPassword)) {
          keyPassword = keyStorePassword;
          LOGGER.trace("used same password for key as for store");
        }

        keyStore = KeystoreFactory.getKeyStore(keyStorePath, keyStorePassword,
                keystoreType);
      }
      return new SslHandler(getSslEngine(keyStore, keyPassword));
    }
    else {
      return null;
    }
  }

  /**
   * Initialize the {@link javax.net.ssl.SSLEngine} for the
   * {@link io.netty.handler.ssl.SslHandler}. Anytime the engine is null or no
   * more valid. Otherwise the previous created will be reused.
   * 
   * @param keyPassword
   * @param keyStore
   * @return
   */
  public static SSLEngine getSslEngine(final KeyStore keyStore,
          final String keyPassword) {
    if (sslEngine == null || sslEngine.isInboundDone()
            || sslEngine.isOutboundDone()) {
      try {
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keyPassword.toCharArray());

        final SSLContext sslc = SSLContext.getInstance("TLSv1");
        sslc.init(kmf.getKeyManagers(), null, null);

        final SSLEngine sslEngine = sslc.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);

        SslHandlerFactory.sslEngine = sslEngine;
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("could not build SSL Engine", e);
      } catch (UnrecoverableKeyException e) {
        throw new RuntimeException("could not build SSL Engine", e);
      } catch (KeyStoreException e) {
        throw new RuntimeException("could not build SSL Engine", e);
      } catch (KeyManagementException e) {
        throw new RuntimeException("could not build SSL Engine", e);
      }
    }
    return sslEngine;
  }
}
