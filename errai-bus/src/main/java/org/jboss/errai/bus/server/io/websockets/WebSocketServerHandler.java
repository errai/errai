/*
* Copyright 2010 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License.  You may obtain a copy of the License at:
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
* License for the specific language governing permissions and limitations
* under the License.
*/
package org.jboss.errai.bus.server.io.websockets;

import io.netty.buffer.ChannelBuffers;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.server.JSONDecoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * The working prototype ErraiBus Websocket Server.
 */
public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
  public static final String SESSION_ATTR_WS_STATUS = "WebSocketStatus";

  public static final String WEBSOCKET_AWAIT_ACTIVATION = "AwaitingActivation";
  public static final String WEBSOCKET_ACTIVE = "Active";

  public static final String WEBSOCKET_PATH = "/websocket.bus";

  private final Map<Channel, QueueSession> activeChannels = new ConcurrentHashMap<Channel, QueueSession>();

  private WebSocketServerHandshaker handshaker = null;
  private ErraiService svc;

  public WebSocketServerHandler(ErraiService bus) {
    this.svc = bus;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object msg = e.getMessage();
    if (msg instanceof HttpRequest) {
      handleHttpRequest(ctx, (HttpRequest) msg);
    }
    else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    }
  }

  private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
    // Allow only GET methods.
    if (req.getMethod() != GET) {
      sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
      return;
    }

    // Handshake
    WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
            this.getWebSocketLocation(req), null, false);
    this.handshaker = wsFactory.newHandshaker(req);
    if (this.handshaker == null) {
      wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
    }
    else {
      this.handshaker.performOpeningHandshake(ctx.getChannel(), req);
    }
  }

  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

    // Check for closing frame
    if (frame instanceof CloseWebSocketFrame) {
      activeChannels.remove(ctx.getChannel());

      this.handshaker.performClosingHandshake(ctx.getChannel(), (CloseWebSocketFrame) frame);
      return;
    }
    else if (frame instanceof PingWebSocketFrame) {
      ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
      return;
    }
    else if (!(frame instanceof TextWebSocketFrame)) {
      throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
              .getName()));
    }

    @SuppressWarnings("unchecked") EJObject val = JSONDecoder.decode(((TextWebSocketFrame) frame).getText()).isObject();

    QueueSession session;

    // this is not an active channel.
    if (!activeChannels.containsKey(ctx.getChannel())) {
      String commandType =  val.get(MessageParts.CommandType.name()).isString().stringValue();

      // this client apparently wants to connect.
      if (BusCommands.ConnectToQueue.name().equals(commandType)) {
        String sessionKey = val.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();

        // has this client already attempted a connection, and is in a wait verify state
        if (sessionKey != null && (session = svc.getBus().getSessionBySessionId(sessionKey)) != null) {
          if (session.hasAttribute(SESSION_ATTR_WS_STATUS) &&
                  WEBSOCKET_ACTIVE.equals(session.getAttribute(String.class, SESSION_ATTR_WS_STATUS))) {

            // open the channel
            activeChannels.put(ctx.getChannel(), session);

            // set the session queue into direct channel mode.
            svc.getBus().getQueueBySession(sessionKey).setDirectSocketChannel(ctx.getChannel());

            // remove the web socket token so it cannot be re-used for authentication.
            session.removeAttribute(MessageParts.WebSocketToken.name());
            session.removeAttribute(SESSION_ATTR_WS_STATUS);

            return;
          }

          // check the activation key matches what we have in the ssession.
          String activationKey = session.getAttribute(String.class, MessageParts.WebSocketToken.name());
          if (activationKey == null || !activationKey.equals(val.get(MessageParts.WebSocketToken.name()).isString().stringValue())) {
            // nope. go away!
            sendMessage(ctx, getFailedNegotiation("bad negotiation key"));
          }
          else {
            // the key matches. now we send the reverse challenge to prove this client is actually
            // already talking to the bus over the COMET channel.
            String reverseToken = SecureHashUtil.nextSecureHash("SHA-256");
            session.setAttribute(MessageParts.WebSocketToken.name(), reverseToken);
            session.setAttribute(SESSION_ATTR_WS_STATUS, WEBSOCKET_AWAIT_ACTIVATION);

            // send the challenge.
            sendMessage(ctx, getReverseChallenge(reverseToken));
            return;
          }

          sendMessage(ctx, getSuccessfulNegotiation());
        }
        else {
          sendMessage(ctx, getFailedNegotiation("bad session id"));
        }
      }
      else {
        sendMessage(ctx, getFailedNegotiation("bad command"));
      }

    }
    else {
      // this is an active session. send the message.

      session = activeChannels.get(ctx.getChannel());
      Message msg = MessageFactory.createCommandMessage(session, ((TextWebSocketFrame) frame).getText());
      svc.store(msg);
    }
  }

  private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
    // Generate an error page if response status code is not OK (200).
    if (res.getStatus().getCode() != 200) {
      res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
      setContentLength(res, res.getContent().readableBytes());
    }

    // Send the response and close the connection if necessary.
    ChannelFuture f = ctx.getChannel().write(res);
    if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    //noinspection ThrowableResultOfMethodCallIgnored
    e.getCause().printStackTrace();
    e.getChannel().close();
  }

  private String getWebSocketLocation(HttpRequest req) {
    return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
  }

  public static void sendMessage(ChannelHandlerContext ctx, String message) {
    ctx.getChannel().write(new TextWebSocketFrame(message));
  }

  private static String getFailedNegotiation(String error) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommands.WebsocketNegotiationFailed.name() + "\"," +
            "\"" + MessageParts.ErrorMessage.name() + "\":\"" + error + "\"}]";
  }

  private static String getSuccessfulNegotiation() {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommands.WebsocketChannelOpen.name() + "\"}]";
  }

  private static String getReverseChallenge(String token) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommands.WebsocketChannelVerify.name() + "\",\"" + MessageParts.WebSocketToken + "\":\"" +
            token + "\"}]";
  }
}














