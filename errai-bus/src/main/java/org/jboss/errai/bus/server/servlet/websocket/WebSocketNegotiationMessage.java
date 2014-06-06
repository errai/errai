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