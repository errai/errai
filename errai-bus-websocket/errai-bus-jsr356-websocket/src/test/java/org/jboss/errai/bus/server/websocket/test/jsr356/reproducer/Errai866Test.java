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

package org.jboss.errai.bus.server.websocket.test.jsr356.reproducer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.jboss.errai.bus.server.websocket.jsr356.ErraiWebSocketEndpoint;
import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiWebSocketChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Errai866Test {
  
  private ErraiWebSocketEndpoint wsEndpoint;
  
  private Map<String, Object> userProperties;
  
  @Mock
  private Session wsSession;
  
  @Mock
  private EndpointConfig config;

  @Mock
  private HttpSession httpSession;
  
  @Mock
  private ErraiChannelFactory channelFactory;

  @Mock
  private ErraiWebSocketChannel channel;
  
  @Before
  public void setup() {
    wsEndpoint = new ErraiWebSocketEndpoint();
    userProperties = new HashMap<String, Object>();
    userProperties.put(HttpSession.class.getName(), httpSession);
    ErraiChannelFactory.registerDelegate(channelFactory);

    when(wsSession.getId()).thenReturn("mock-session-id");
    when(config.getUserProperties()).thenReturn(userProperties);
    when(channelFactory.buildWebsocketChannel(any(Session.class), any(HttpSession.class)))
      .thenReturn(channel);
    doThrow(new RuntimeException("bad payload")).when(channel).doErraiMessage(any(String.class));
    
    wsEndpoint.onOpen(wsSession, config);
  }
  
  @Test
  public void exceptionNotThrownForEmptyMessage() throws Exception {
    wsEndpoint.onMessage("", wsSession);
  }
  
  @Test(expected=RuntimeException.class)
  public void exceptionThrownForNonEmptyMessage() throws Exception {
    wsEndpoint.onMessage("malformed", wsSession);
  }

}
