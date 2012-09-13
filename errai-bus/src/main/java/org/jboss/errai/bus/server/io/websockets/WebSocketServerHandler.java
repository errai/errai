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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ExceptionEvent;
import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelUpstreamHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJString;
import org.jboss.errai.marshalling.server.JSONDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

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

  public WebSocketServerHandler(final ErraiService bus) {
    this.svc = bus;
  }

  @Override
  public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
    final Object msg = e.getMessage();
    if (msg instanceof HttpRequest) {
      handleHttpRequest(ctx, (HttpRequest) msg);
    }
    else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    }
  }

  private void handleHttpRequest(final ChannelHandlerContext ctx, final HttpRequest req) throws Exception {
    // Allow only GET methods.
    if (req.getMethod() != GET) {
      sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
      return;
    }

    // Handshake
    final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
        this.getWebSocketLocation(req), null, false);
    this.handshaker = wsFactory.newHandshaker(req);
    if (this.handshaker == null) {
      wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
    }
    else {
      this.handshaker.handshake(ctx.getChannel(), req);
    }
  }

  private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {

    // Check for closing frame
    if (frame instanceof CloseWebSocketFrame) {
      activeChannels.remove(ctx.getChannel());

      this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
      return;
    }
    if (frame instanceof PingWebSocketFrame) {
      ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
      return;
    }
    if (!(frame instanceof TextWebSocketFrame)) {
      throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
          .getName()));
    }

    @SuppressWarnings("unchecked") final EJObject val = JSONDecoder.decode(((TextWebSocketFrame) frame).getText()).isObject();

    final QueueSession session;

    // this is not an active channel.
    if (!activeChannels.containsKey(ctx.getChannel())) {
      final String commandType = val.get(MessageParts.CommandType.name()).isString().stringValue();

      // this client apparently wants to connect.
      if (BusCommands.ConnectToQueue.name().equals(commandType)) {
        final String sessionKey = val.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();

        // has this client already attempted a connection, and is in a wait verify state
        if (sessionKey != null && (session = svc.getBus().getSessionBySessionId(sessionKey)) != null) {
          final LocalContext localContext = LocalContext.get(session);

          if (localContext.hasAttribute(SESSION_ATTR_WS_STATUS) &&
              WEBSOCKET_ACTIVE.equals(localContext.getAttribute(String.class, SESSION_ATTR_WS_STATUS))) {

            // open the channel
            activeChannels.put(ctx.getChannel(), session);

            // set the session queue into direct channel mode.
            svc.getBus().getQueueBySession(sessionKey).setDirectSocketChannel(new NettyQueueChannel(ctx.getChannel()));

            // remove the web socket token so it cannot be re-used for authentication.
            localContext.removeAttribute(MessageParts.WebSocketToken.name());
            localContext.removeAttribute(SESSION_ATTR_WS_STATUS);

            return;
          }

          // check the activation key matches.
          final EJString activationKey = val.get(MessageParts.WebSocketToken.name()).isString();
          if (activationKey == null || !WebSocketTokenManager.verifyOneTimeToken(session, activationKey.stringValue())) {
            // nope. go away!
            sendMessage(ctx, getFailedNegotiation("bad negotiation key"));
          }
          else {
            // the key matches. now we send the reverse challenge to prove this client is actually
            // already talking to the bus over the COMET channel.
            final String reverseToken = WebSocketTokenManager.getNewOneTimeToken(session);
            localContext.setAttribute(MessageParts.WebSocketToken.name(), reverseToken);
            localContext.setAttribute(SESSION_ATTR_WS_STATUS, WEBSOCKET_AWAIT_ACTIVATION);

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

      final Message msg = MessageFactory.createCommandMessage(session, ((TextWebSocketFrame) frame).getText());

      msg.setResource(HttpServletRequest.class.getName(), new SyntheticHttpServletRequest());

      svc.store(msg);
    }
  }

  private void sendHttpResponse(final ChannelHandlerContext ctx, final HttpRequest req, final HttpResponse res) {
    // Generate an error page if response status code is not OK (200).
    if (res.getStatus().getCode() != 200) {
      res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
      setContentLength(res, res.getContent().readableBytes());
    }

    // Send the response and close the connection if necessary.
    final ChannelFuture f = ctx.getChannel().write(res);
    if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
    //noinspection ThrowableResultOfMethodCallIgnored
    e.getCause().printStackTrace();
    e.getChannel().close();
  }

  private String getWebSocketLocation(final HttpRequest req) {
    return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
  }

  public static void sendMessage(final ChannelHandlerContext ctx, final String message) {
    ctx.getChannel().write(new TextWebSocketFrame(message));
  }

  private static String getFailedNegotiation(final String error) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
        + BusCommands.WebsocketNegotiationFailed.name() + "\"," +
        "\"" + MessageParts.ErrorMessage.name() + "\":\"" + error + "\"}]";
  }

  private static String getSuccessfulNegotiation() {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
        + BusCommands.WebsocketChannelOpen.name() + "\"}]";
  }

  private static String getReverseChallenge(final String token) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
        + BusCommands.WebsocketChannelVerify.name() + "\",\"" + MessageParts.WebSocketToken + "\":\"" +
        token + "\"}]";
  }

  public void stop() {
    for (final Channel channel : activeChannels.keySet()) {
      channel.close();
    }
  }

  private static class SyntheticHttpServletRequest implements HttpServletRequest {
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, String[]> parameters = new HashMap<String, String[]>();

    @Override
    public Object getAttribute(final String name) {
      return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
      return new Enumeration<String>() {
        private final Iterator<String> stringIterator = attributes.keySet().iterator();

        @Override
        public boolean hasMoreElements() {
          return stringIterator.hasNext();
        }

        @Override
        public String nextElement() {
          return stringIterator.next();
        }
      };
    }

    @Override
    public String getCharacterEncoding() {
      return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
    }

    @Override
    public int getContentLength() {
      return 0;
    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      return null;
    }

    @Override
    public String getParameter(final String name) {
      final String[] parms = parameters.get(name);
      if (parms == null) {
        return null;
      }
      else {
        return parms[0];
      }
    }

    @Override
    public Enumeration<String> getParameterNames() {
      return new Enumeration<String>() {
        private final Iterator<String> stringIterator = parameters.keySet().iterator();

        @Override
        public boolean hasMoreElements() {
          return stringIterator.hasNext();
        }

        @Override
        public String nextElement() {
          return stringIterator.next();
        }
      };
    }

    @Override
    public String[] getParameterValues(final String name) {
      return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
      return parameters;
    }

    @Override
    public String getProtocol() {
      return null;
    }

    @Override
    public String getScheme() {
      return null;
    }

    @Override
    public String getServerName() {
      return null;
    }

    @Override
    public int getServerPort() {
      return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
      return null;
    }

    @Override
    public String getRemoteAddr() {
      return null;
    }

    @Override
    public String getRemoteHost() {
      return null;
    }

    @Override
    public void setAttribute(final String name, final Object o) {
      attributes.put(name, o);
    }

    @Override
    public void removeAttribute(final String name) {
      attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
      return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
      return null;
    }

    @Override
    public boolean isSecure() {
      return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
      return null;
    }

    @Override
    public String getRealPath(final String path) {
      return null;
    }

    @Override
    public int getRemotePort() {
      return 0;
    }

    @Override
    public String getLocalName() {
      return null;
    }

    @Override
    public String getLocalAddr() {
      return null;
    }

    @Override
    public int getLocalPort() {
      return 0;
    }

    @Override
    public ServletContext getServletContext() {
      return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
      return null;
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IllegalStateException {
      return null;
    }

    @Override
    public boolean isAsyncStarted() {
      return false;
    }

    @Override
    public boolean isAsyncSupported() {
      return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
      return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
      return null;
    }

    @Override
    public String getAuthType() {
      return null;
    }

    @Override
    public Cookie[] getCookies() {
      return new Cookie[0];
    }

    @Override
    public long getDateHeader(final String name) {
      return 0;
    }

    @Override
    public String getHeader(final String name) {
      return null;
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
      return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
      return null;
    }

    @Override
    public int getIntHeader(final String name) {
      return 0;
    }

    @Override
    public String getMethod() {
      return null;
    }

    @Override
    public String getPathInfo() {
      return null;
    }

    @Override
    public String getPathTranslated() {
      return null;
    }

    @Override
    public String getContextPath() {
      return null;
    }

    @Override
    public String getQueryString() {
      return null;
    }

    @Override
    public String getRemoteUser() {
      return null;
    }

    @Override
    public boolean isUserInRole(final String role) {
      return false;
    }

    @Override
    public Principal getUserPrincipal() {
      return null;
    }

    @Override
    public String getRequestedSessionId() {
      return null;
    }

    @Override
    public String getRequestURI() {
      return null;
    }

    @Override
    public StringBuffer getRequestURL() {
      return null;
    }

    @Override
    public String getServletPath() {
      return null;
    }

    @Override
    public HttpSession getSession(final boolean create) {
      return null;
    }

    @Override
    public HttpSession getSession() {
      return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
      return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
      return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
      return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
      return false;
    }

    @Override
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
      return false;
    }

    @Override
    public void login(final String username, final String password) throws ServletException {
    }

    @Override
    public void logout() throws ServletException {
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
      return null;
    }

    @Override
    public Part getPart(final String name) throws IOException, ServletException {
      return null;
    }
  }
}














