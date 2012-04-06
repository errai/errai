package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.as.websockets.WebSocket;
import org.jboss.as.websockets.servlet.WebSocketServlet;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mike Brock
 */
public class JBossAS7WebSocketServlet extends WebSocketServlet {

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

  private static final String WEBSOCKET_SESSION_ALIAS = "Websocket:Errai:SessionAlias";


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

  private static class SimpleEventChannelWrapped implements QueueChannel {
    private final WebSocket socket;

    public SimpleEventChannelWrapped(WebSocket socket) {
      this.socket = socket;
    }

    @Override
    public boolean isConnected() {
      return true;
    }

    @Override
    public void write(String data) throws IOException {
      socket.writeTextFrame(data);
    }
  }

  @Override
  protected void onSocketOpened(HttpEvent event, WebSocket socket) throws IOException {
  }

  @Override
  protected void onSocketClosed(HttpEvent event) throws IOException {
  }


  @Override
  protected void onReceivedTextFrame(HttpEvent event, final WebSocket socket) throws IOException {
    final String text = socket.readTextFrame();

    final QueueSession session = sessionProvider.getSession(event.getHttpServletRequest().getSession(),
            event.getHttpServletRequest().getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

    if (text.length() == 0) return;

    @SuppressWarnings("unchecked") EJObject val = JSONDecoder.decode(text).isObject();


    // this is not an active channel.
    if (!session.hasAttribute(WEBSOCKET_SESSION_ALIAS)) {
      String commandType = val.get(MessageParts.CommandType.name()).isString().stringValue();

      // this client apparently wants to connect.
      if (BusCommands.ConnectToQueue.name().equals(commandType)) {
        String sessionKey = val.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();

        final QueueSession cometSession;

        // has this client already attempted a connection, and is in a wait verify state
        if (sessionKey != null && (cometSession = service.getBus().getSessionBySessionId(sessionKey)) != null) {
          if (cometSession.hasAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS) &&
                  WebSocketServerHandler.WEBSOCKET_ACTIVE.equals(cometSession.getAttribute(String.class, WebSocketServerHandler.SESSION_ATTR_WS_STATUS))) {

            // set the session queue into direct channel mode.
            service.getBus().getQueue(cometSession).setDirectSocketChannel(new QueueChannel() {
              @Override
              public boolean isConnected() {
                return cometSession.isValid();
              }

              @Override
              public void write(String data) throws IOException {
                socket.writeTextFrame(data);
              }
            });

            session.setAttribute(WEBSOCKET_SESSION_ALIAS, cometSession);

            // associate the new WebSocket session with the the old comet session.
            //   service.getBus().associateNewQueue(cometSession, session);

            // remove the web socket token so it cannot be re-used for authentication.
            cometSession.removeAttribute(MessageParts.WebSocketToken.name());
            cometSession.removeAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS);

            return;
          }

          // check the activation key matches what we have in the ssession.
          String activationKey = cometSession.getAttribute(String.class, MessageParts.WebSocketToken.name());
          if (activationKey == null || !activationKey.equals(val.get(MessageParts.WebSocketToken.name()).isString().stringValue())) {
            // nope. go away!
            sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad negotiation key"));
          }
          else {
            // the key matches. now we send the reverse challenge to prove this client is actually
            // already talking to the bus over the COMET channel.
            String reverseToken = SecureHashUtil.nextSecureHash("SHA-256");
            cometSession.setAttribute(MessageParts.WebSocketToken.name(), reverseToken);
            cometSession.setAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS, WebSocketServerHandler.WEBSOCKET_AWAIT_ACTIVATION);

            // send the challenge.
            sendMessage(new SimpleEventChannelWrapped(socket), getReverseChallenge(reverseToken));
            return;
          }

          sendMessage(new SimpleEventChannelWrapped(socket), getSuccessfulNegotiation());
        }
        else {
          sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad session id"));
        }
      }
      else {
        sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad command"));
      }

    }
    else {
      // this is an active session. send the message.;

      Message msg = MessageFactory.createCommandMessage(session.getAttribute(QueueSession.class, WEBSOCKET_SESSION_ALIAS), text);
      service.store(msg);
    }
  }

  public static void sendMessage(QueueChannel channel, String message) throws IOException {
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

}