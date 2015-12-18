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

import io.netty.handler.ssl.SslHandler;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for an {@link io.netty.handler.ssl.SslHandler} to provide WSS support
 * for websocket sideband server.
 * 
 * @author Michel Werren
 */
public class SslHandlerFactory {
  private static KeyStore keyStore = null;
  private static SSLEngine sslEngine = null;
  private static String keyPassword = null;

  private static final Logger log = LoggerFactory.getLogger(SslHandlerFactory.class.getName());

  /**
   * Build a {@link io.netty.handler.ssl.SslHandler} when the side band server
   * is configured to use ssl for websocket.
   * 
   * @param esc
   * @return the ssl handler, never null.
   */
  public static SslHandler buildSslHandler(ErraiServiceConfigurator esc) {
    keyPassword = StringUtils.isEmpty(keyPassword) ? ErraiConfigAttribs.WEB_SOCKET_KEY_PASSWORD.get(esc) : keyPassword;
    if (keyStore == null) {
      final String keyStorePath = ErraiConfigAttribs.WEB_SOCKET_KEYSTORE.get(esc);
      final String keystoreType = ErraiConfigAttribs.WEB_SOCKET_KEYSTORE_TYPE.get(esc);

      if (StringUtils.isEmpty(keyStorePath)) {
        throw new IllegalStateException(
                "when ssl is activated for the sideband server, key store information is necessary");
      }
      String keyStorePassword = ErraiConfigAttribs.WEB_SOCKET_KEYSTORE_PASSWORD.get(esc);

      if (StringUtils.isEmpty(keyStorePassword)) {
        throw new IllegalStateException(
                "keystore configured for sideband websocket server, but missing keystore password");
      }

      if (StringUtils.isEmpty(keyPassword)) {
        keyPassword = keyStorePassword;
      }

      keyStore = KeystoreFactory.getKeyStore(keyStorePath, keyStorePassword, keystoreType);
    }
    return new SslHandler(getSslEngine(keyStore, keyPassword));
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
  public static SSLEngine getSslEngine(final KeyStore keyStore, final String keyPassword) {
    if (sslEngine == null || sslEngine.isInboundDone() || sslEngine.isOutboundDone()) {
      try {
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keyPassword.toCharArray());

        final SSLContext sslc = SSLContext.getInstance("TLSv1");
        sslc.init(kmf.getKeyManagers(), null, null);

        final SSLEngine sslEngine = sslc.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);

        SslHandlerFactory.sslEngine = sslEngine;
      } catch (Exception e) {
        throw new RuntimeException("could not build SSL Engine", e);
      }
    }
    return sslEngine;
  }
}
