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

package org.jboss.errai.bus.server.servlet.websocket;

import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * @author Michel Werren
 */
public class WebSocketNegotiationMessage {
  
  private WebSocketNegotiationMessage() {};

  public static String getFailedNegotiation(final String error) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \""
            + MessageParts.CommandType.name() + "\":\""
            + BusCommand.WebsocketNegotiationFailed.name() + "\"," + "\""
            + MessageParts.ErrorMessage.name() + "\":\"" + error + "\"}]";
  }

  public static String getSuccessfulNegotiation() {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \""
            + MessageParts.CommandType.name() + "\":\""
            + BusCommand.WebsocketChannelOpen.name() + "\"}]";
  }

  public static String getReverseChallenge(final String token) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \""
            + MessageParts.CommandType.name() + "\":\""
            + BusCommand.WebsocketChannelVerify.name() + "\",\""
            + MessageParts.WebSocketToken + "\":\"" + token + "\"}]";
  }
}
