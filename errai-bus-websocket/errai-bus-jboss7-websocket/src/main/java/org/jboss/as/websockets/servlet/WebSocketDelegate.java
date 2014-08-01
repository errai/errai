package org.jboss.as.websockets.servlet;

import org.jboss.as.websockets.Frame;
import org.jboss.as.websockets.WebSocket;
import org.jboss.websockets.oio.OioWebSocket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
class WebSocketDelegate implements WebSocket {
  protected HttpServletRequest request;
  protected OioWebSocket delegate;

  public WebSocketDelegate(HttpServletRequest request, OioWebSocket delegate) {
    this.request = request;
    this.delegate = delegate;
  }

  public HttpSession getHttpSession() {
    return request.getSession();
  }

  public HttpServletRequest getServletRequest() {
    return request;
  }

  public String getSocketID() {
    return delegate.getSocketID();
  }

  public Frame readFrame() throws IOException {
    return delegate.readFrame();
  }

  public void writeFrame(Frame frame) throws IOException {
    delegate.writeFrame(frame);
  }

  public void closeSocket() throws IOException {
    delegate.closeSocket();
  }
}