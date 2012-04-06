package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.as.websockets.Handshake;
import org.jboss.as.websockets.buffer.BufferColor;
import org.jboss.as.websockets.buffer.TransmissionBuffer;
import org.jboss.as.websockets.protocol.ietf00.Ietf00Handshake;
import org.jboss.as.websockets.protocol.ietf07.Ietf07Handshake;
import org.jboss.as.websockets.protocol.ietf08.Ietf08Handshake;
import org.jboss.as.websockets.protocol.ietf13.Ietf13Handshake;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.io.QueueChannel;
import org.jboss.errai.bus.server.io.websockets.WebSocketServerHandler;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEventServlet;
import org.jboss.servlet.http.UpgradableHttpServletResponse;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

/**
 * @author Mike Brock
 */
public class JBossAS7WebSocketServlet extends HttpServlet implements HttpEventServlet {

  /* New and configured errai service */
  protected ErraiService service;

  /* A default Http session provider */
  protected SessionProvider<HttpSession> sessionProvider;

  protected volatile ClassLoader contextClassLoader;

  // protected Logger log = LoggerFactory.getLogger(getClass());

  public enum ConnectionPhase {
    NORMAL, CONNECTING, DISCONNECTING, UNKNOWN
  }

  static {
    ScannerSingleton.class.getName();
  }

  public static ConnectionPhase getConnectionPhase(final HttpServletRequest request) {
    if (request.getHeader("phase") == null) return ConnectionPhase.NORMAL;
    else {
      String phase = request.getHeader("phase");
      if ("connection".equals(phase)) {
        return ConnectionPhase.CONNECTING;
      }
      if ("disconnect".equals(phase)) {
        return ConnectionPhase.DISCONNECTING;
      }

      return ConnectionPhase.UNKNOWN;
    }
  }

  private static final List<Handshake> websocketHandshakes;

  static {
    final List<Handshake> handshakeList = new ArrayList<Handshake>();
    handshakeList.add(new Ietf00Handshake());
    handshakeList.add(new Ietf07Handshake());
    handshakeList.add(new Ietf08Handshake());
    handshakeList.add(new Ietf13Handshake());

    websocketHandshakes = Collections.unmodifiableList(handshakeList);
  }

  private static final String SESSION_READ_BUFFER_KEY = "JBoss:Experimental:WebsocketReadBuffer";
  private static final String SESSION_WRITE_STREAM_KEY = "JBoss:Experimental:WebsocketWriteStream";


  public void event(final HttpEvent event) throws IOException, ServletException {
    System.out.println("Event:" + event);
    switch (event.getType()) {
      case BEGIN:
        event.setTimeout(20000);
        final HttpServletRequest request = event.getHttpServletRequest();
        final HttpServletResponse response = event.getHttpServletResponse();
        System.out.println("Begin WebSocket Handshake ...");
        if (response instanceof UpgradableHttpServletResponse) {
          for (Handshake handshake : websocketHandshakes) {
            System.out.println("Checking handshake ... " + request);
            if (handshake.matches(request)) {
              System.out.println("Using handshake: " + handshake.getClass().getName());

              handshake.generateResponse(event);

              response.setHeader("Upgrade", "websocket");
              response.setHeader("Connection", "Upgrade");

              ((UpgradableHttpServletResponse) response).sendUpgrade();

              createSessionBufferEntry(event);
              notifyConnectionBegin(event);
            }
          }
        }
        else {
          throw new IllegalStateException("cannot upgrade connection");
        }
        break;
      case END:
        break;
      case ERROR:
        event.close();
        break;
      case EVENT:
      case READ:
        while (event.isReadReady()) {
          handleReceivedEvent(event, readFrame(event, event.getHttpServletRequest().getInputStream()));
        }
        break;

      case TIMEOUT:
        event.resume();
        break;

    }
  }


  private static final byte FRAME_FIN = Byte.MIN_VALUE;
  private static final byte FRAME_OPCODE = 0x0F;
  private static final byte FRAME_MASKED = Byte.MIN_VALUE;
  private static final byte FRAME_LENGTH = 127;

  private static final int OPCODE_CONTINUATION = 0;
  private static final int OPCODE_TEXT = 1;
  private static final int OPCODE_BINARY = 2;
  private static final int OPCODE_CONNECTION_CLOSE = 3;
  private static final int OPCODE_PING = 4;
  private static final int OPCODE_PONG = 5;

  public static String readFrame(HttpEvent event, InputStream stream) throws IOException {
    final StringBuilder payloadBuffer = new StringBuilder();
    int b;
    boolean last;

    b = stream.read();
    last = (b & FRAME_FIN) != 0;

    int opcode = (b & FRAME_OPCODE);

    b = stream.read();

    boolean frameMasked = (b & FRAME_MASKED) != 0;

    int payloadLength = (b & FRAME_LENGTH);
    if (payloadLength == 126) {
      payloadLength = ((stream.read() & 0xFF) << 8) +
              (stream.read() & 0xFF);
    }

    final int[] frameMaskingKey = new int[4];

    if (frameMasked) {
      frameMaskingKey[0] = stream.read();
      frameMaskingKey[1] = stream.read();
      frameMaskingKey[2] = stream.read();
      frameMaskingKey[3] = stream.read();
    }

    System.out.println("WS_FRAME(opcode=" + opcode + ";frameMasked=" + frameMasked + ";payloadLength="
            + payloadLength + ";frameMask=" + Arrays.toString(frameMaskingKey) + ")");

    switch (opcode) {
      case OPCODE_TEXT:
        int read = 0;
        if (frameMasked) {
          do {
            int r = stream.read();
            payloadBuffer.append(((char) ((r ^ frameMaskingKey[read % 4]) & 127)));
          }
          while (++read < payloadLength);
        }
        else {
          // support unmasked frames for testing.

          do {
            payloadBuffer.append((char) stream.read());
          }
          while (++read < payloadLength);
        }
        break;
      case OPCODE_CONNECTION_CLOSE:
        event.close();
        break;

      case OPCODE_PING:
      case OPCODE_PONG:
        break;

      case OPCODE_BINARY:
        // binary transmission not supported
        break;

    }

    return payloadBuffer.toString();
  }

  private static final byte[] randomSeed = SecureHashUtil.nextSecureHash("SHA-1",
          String.valueOf(System.nanoTime())).getBytes();
  private static int counter = (int) (System.currentTimeMillis() % 10000);

  public static byte getMask() {
    return randomSeed[++counter % (randomSeed.length - 1)];
  }

  private static byte[] mask = {getMask(), getMask(), getMask(), getMask()};

  public static void writeWebSocketFrame(final OutputStream stream, final String txt) throws IOException {
    byte[] strBytes = txt.getBytes("UTF-8");
    boolean big = strBytes.length > 125;

    int i = 0;
    stream.write(-127);
    if (big) {
      stream.write(-2);
      stream.write(((strBytes.length >> 8) & 0xFF));
      stream.write(((strBytes.length) & 0xFF));
    }
    else {
      stream.write(-128 | (strBytes.length & 127));
    }

    stream.write(mask[0]);
    stream.write(mask[1]);
    stream.write(mask[2]);
    stream.write(mask[3]);


    int len = strBytes.length;
    for (int j = 0; j < len; j++) {
      stream.write((strBytes[j] ^ mask[j % 4]));
    }

    stream.flush();
  }


  private static void createSessionBufferEntry(HttpEvent event) throws IOException {
    final HttpSession session = event.getHttpServletRequest().getSession();

    session.setAttribute(SESSION_READ_BUFFER_KEY, BufferColor.getNewColor());
    session.setAttribute(SESSION_WRITE_STREAM_KEY, event.getHttpServletResponse().getOutputStream());
  }

  public static BufferColor getSessionBufferEntry(HttpEvent event) {
    return getSessionBufferEntry(event.getHttpServletRequest().getSession());
  }

  public static BufferColor getSessionBufferEntry(HttpSession session) {
    return (BufferColor) session.getAttribute(SESSION_READ_BUFFER_KEY);
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    System.setProperty("org.jboss.errai.websocket_servlet", "true");

    init(config.getServletContext(), config.getInitParameter("service-locator"));
  }

  /**
   * Common initialization logic that works for both Servlets and Filters.
   *
   * @param context             The ServletContext of the web application.
   * @param serviceLocatorClass The value of the (Servlet or Filter) init parameter
   *                            <code>"service-locator"</code>. If specified, it must be the
   *                            fully-qualified name of a class that implements
   *                            {@link ServiceLocator}. If null, the service locator is built by a
   *                            call to {@link #buildService()}.
   */
  protected void init(final ServletContext context, String serviceLocatorClass) {
    service = (ErraiService) context.getAttribute("errai");
    if (null == service) {
      synchronized (context) {
        // Build or lookup service
        if (serviceLocatorClass != null) {
          // locate externally created service instance, i.e. CDI
          try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> aClass = loader.loadClass(serviceLocatorClass);
            ServiceLocator locator = (ServiceLocator) aClass.newInstance();
            this.service = locator.locateService();
          }
          catch (Exception e) {
            throw new RuntimeException("Failed to create service", e);
          }
        }
        else {
          // create a service instance manually
          this.service = buildService();
        }

        contextClassLoader = Thread.currentThread().getContextClassLoader();

        service.getConfiguration().getResourceProviders()
                .put("errai.experimental.classLoader", new ResourceProvider<ClassLoader>() {
                  @Override
                  public ClassLoader get() {
                    return contextClassLoader;
                  }
                });

        service.getConfiguration().getResourceProviders()
                .put("errai.experimental.servletContext", new ResourceProvider<ServletContext>() {
                  @Override
                  public ServletContext get() {
                    return context;
                  }
                });

        // store it in servlet context
        context.setAttribute("errai", service);
      }
    }

    sessionProvider = service.getSessionProvider();
  }

  @Override
  public void destroy() {
    service.stopService();
  }

  @SuppressWarnings({"unchecked"})
  protected ErraiService<HttpSession> buildService() {
    return Guice.createInjector(new AbstractModule() {
      @Override
      @SuppressWarnings({"unchecked"})
      public void configure() {
        bind(ErraiService.class).to(ErraiServiceImpl.class);
        bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
        bind(MessageBus.class).to(ServerMessageBusImpl.class);
        bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
      }
    }).getInstance(ErraiService.class);
  }


  /**
   * Writes the message to the output stream
   *
   * @param stream - the stream to write to
   * @param m      - the message to write to the stream
   * @throws java.io.IOException - is thrown if any input/output errors occur while writing to the stream
   */
  public static void writeToOutputStream(OutputStream stream, MarshalledMessage m) throws IOException {
    stream.write('[');

    if (m.getMessage() == null) {
      stream.write('n');
      stream.write('u');
      stream.write('l');
      stream.write('l');
    }
    else {
      for (byte b : ((String) m.getMessage()).getBytes()) {
        stream.write(b);
      }
    }
    stream.write(']');

  }


  protected void writeExceptionToOutputStream(HttpServletResponse httpServletResponse
          , final
  Throwable t) throws IOException {
    httpServletResponse.setHeader("Cache-Control", "no-cache");
    httpServletResponse.addHeader("Payload-Size", "1");
    httpServletResponse.setContentType("application/json");
    OutputStream stream = httpServletResponse.getOutputStream();

    stream.write('[');

    writeToOutputStream(stream, new MarshalledMessage() {
      @Override
      public String getSubject() {
        return DefaultErrorCallback.CLIENT_ERROR_SUBJECT;
      }

      @Override
      public Object getMessage() {
        StringBuilder b = new StringBuilder("{\"ErrorMessage\":\"").append(t.getMessage()).append("\"," +
                "\"AdditionalDetails\":\"");
        for (StackTraceElement e : t.getStackTrace()) {
          b.append(e.toString()).append("<br/>");
        }

        return b.append("\"}").toString();
      }
    });

    stream.write(']');
    stream.close();
  }


  protected void sendDisconnectDueToSessionExpiry(OutputStream stream) throws IOException {
    writeToOutputStream(stream, new MarshalledMessage() {
      @Override
      public String getSubject() {
        return "ClientBus";
      }

      @Override
      public Object getMessage() {
        return "{\"ToSubject\":\"ClientBus\", \"CommandType\":\"" + BusCommands.SessionExpired + "\"}";
      }
    });
  }


  protected void notifyConnectionBegin(final HttpEvent event) {
    final QueueSession session = sessionProvider.getSession(event.getHttpServletRequest().getSession(),
            event.getHttpServletRequest().getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

    System.out.println("notifyConnectionBegin() SessionID=" + session.getSessionId());
  }

  protected void handleReceivedEvent(final HttpEvent event, final String text) throws IOException {
    final QueueSession session = sessionProvider.getSession(event.getHttpServletRequest().getSession(),
            event.getHttpServletRequest().getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

    System.out.println("Websocket_Receieved <<" + text + ">>");

    if (text.length() == 0) return;

    @SuppressWarnings("unchecked") EJObject val = JSONDecoder.decode(text).isObject();


    final QueueChannel channel = new QueueChannel() {
      @Override
      public boolean isConnected() {
        return session.isValid();
      }

      @Override
      public void write(String data) {
        try {
          if (event.isWriteReady()) {
            writeWebSocketFrame(event.getHttpServletResponse().getOutputStream(), data);
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    };

    // this is not an active channel.
    if (!session.hasAttribute("websocket.active")) {
      String commandType = val.get(MessageParts.CommandType.name()).isString().stringValue();

      // this client apparently wants to connect.
      if (BusCommands.ConnectToQueue.name().equals(commandType)) {
        String sessionKey = val.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();
        System.out.println("ConnectToQueue for SeessionKey: " + sessionKey);

        final QueueSession cometSession;

        // has this client already attempted a connection, and is in a wait verify state
        if (sessionKey != null && (cometSession = service.getBus().getSessionBySessionId(sessionKey)) != null) {
          System.out.println("Found the Comet Session!");

          if (cometSession.hasAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS) &&
                  WebSocketServerHandler.WEBSOCKET_ACTIVE.equals(cometSession.getAttribute(String.class, WebSocketServerHandler.SESSION_ATTR_WS_STATUS))) {

            System.out.println("YAY!");

            session.setAttribute("websocket.active", "1");

            // open the channel

            // set the session queue into direct channel mode.
            service.getBus().getQueueBySession(sessionKey).setDirectSocketChannel(channel);

            // remove the web socket token so it cannot be re-used for authentication.
            cometSession.removeAttribute(MessageParts.WebSocketToken.name());
            cometSession.removeAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS);

            return;
          }

          // check the activation key matches what we have in the ssession.
          String activationKey = cometSession.getAttribute(String.class, MessageParts.WebSocketToken.name());
          if (activationKey == null || !activationKey.equals(val.get(MessageParts.WebSocketToken.name()).isString().stringValue())) {
            // nope. go away!
            sendMessage(channel, getFailedNegotiation("bad negotiation key"));
          }
          else {
            System.out.println("MATCHING KEY!");
            // the key matches. now we send the reverse challenge to prove this client is actually
            // already talking to the bus over the COMET channel.
            String reverseToken = SecureHashUtil.nextSecureHash("SHA-256");
            cometSession.setAttribute(MessageParts.WebSocketToken.name(), reverseToken);
            cometSession.setAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS, WebSocketServerHandler.WEBSOCKET_AWAIT_ACTIVATION);

            // send the challenge.
            System.out.println("SENDING REVERSE CHALLENGE: " + reverseToken);
            sendMessage(channel, getReverseChallenge(reverseToken));
            return;
          }

          sendMessage(channel, getSuccessfulNegotiation());
        }
        else {
          sendMessage(channel, getFailedNegotiation("bad session id"));
        }
      }
      else {
        sendMessage(channel, getFailedNegotiation("bad command"));
      }

    }
    else {
      // this is an active session. send the message.

      Message msg = MessageFactory.createCommandMessage(session, text);
      service.store(msg);
    }


    service.store(createCommandMessage(session, text));

    pollForMessages(session, event.getHttpServletRequest(), event.getHttpServletResponse(), false);
  }

  public static void sendMessage(QueueChannel channel, String message) {
    channel.write(message);
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


  private void pollForMessages(QueueSession session, HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse, boolean wait) throws IOException {
    try {
      // note about caching: clients now include a uniquifier in a request parameter called "z"
      // so no-cache headers are now unnecessary.
      httpServletResponse.setContentType("application/json");

      final MessageQueue queue = service.getBus().getQueue(session);
      final ServletOutputStream outputStream = httpServletResponse.getOutputStream();

      if (queue == null) {
        switch (getConnectionPhase(httpServletRequest)) {
          case CONNECTING:
          case DISCONNECTING:
            return;
        }

        sendDisconnectDueToSessionExpiry(outputStream);
        return;
      }

      queue.heartBeat();

      queue.poll(wait, outputStream);

      outputStream.close();
    }
    catch (final Throwable t) {
      t.printStackTrace();
      writeExceptionToOutputStream(httpServletResponse, t);
    }
  }

}