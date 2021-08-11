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

package org.jboss.errai.bus.server.websocket.jsr356.channel;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * Factory for {@link ErraiWebSocketChannel}.
 * 
 * @author Michel Werren
 */
public class ErraiChannelFactory {
  private static final ErraiChannelFactory INSTANCE = new ErraiChannelFactory();

  /**
   * Reference of an alternative factory to delegate the channel creation.
   */
  private static ErraiChannelFactory delegate = null;

  protected ErraiChannelFactory() {
  }

  public static ErraiChannelFactory getInstance() {
    return delegate != null ? delegate : INSTANCE;
  }

  public static void registerDelegate(ErraiChannelFactory delegate) {
    ErraiChannelFactory.delegate = delegate;
  }

  public ErraiWebSocketChannel buildWebsocketChannel(Session websocketSession, HttpSession httpSession) {
    return new DefaultErraiWebSocketChannel(websocketSession, httpSession);
  }
}
