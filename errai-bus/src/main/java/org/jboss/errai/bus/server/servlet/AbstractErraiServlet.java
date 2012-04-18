/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.service.ErraiService;

/**
 * The <tt>AbstractErraiServlet</tt> provides a starting point for creating Http-protocol gateway between the server
 * bus and the client buses.
 */
public abstract class AbstractErraiServlet extends HttpServlet {
  /* New and configured errai service */
  protected ErraiService service;

  /* A default Http session provider */
  protected SessionProvider<HttpSession> sessionProvider;

  public enum ConnectionPhase {
    NORMAL, CONNECTING, DISCONNECTING, UNKNOWN
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

  @Override
  public void init(final ServletConfig config) throws ServletException {
    service = ServletBootstrapUtil.getService(config);
    sessionProvider = service.getSessionProvider();
  }


  public void initAsFilter(final FilterConfig config) throws ServletException {
    service = ServletBootstrapUtil.getService(config);
    sessionProvider = service.getSessionProvider();
  }

  @Override
  public void destroy() {
    service.stopService();
  }

  /**
   * Writes the message to the output stream
   *
   * @param stream - the stream to write to
   * @param m      - the message to write to the stream
   * @throws java.io.IOException - is thrown if any input/output errors occur while writing to the stream
   */
  public static void writeToOutputStream(final OutputStream stream, final MarshalledMessage m) throws IOException {
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


  protected void writeExceptionToOutputStream(
          final HttpServletResponse httpServletResponse,
          final Throwable t) throws IOException {

    httpServletResponse.setHeader("Cache-Control", "no-cache");
    httpServletResponse.addHeader("Payload-Size", "1");
    httpServletResponse.setContentType("application/json");
    final OutputStream stream = httpServletResponse.getOutputStream();

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

  protected void sendDisconnectWithReason(OutputStream stream, final String reason) throws IOException {
    writeToOutputStream(stream, new MarshalledMessage() {
      @Override
      public String getSubject() {
        return "ClientBus";
      }

      @Override
      public Object getMessage() {
        return reason != null ? "{\"ToSubject\":\"ClientBus\", \"CommandType\":\"" + BusCommands.Disconnect + "\"," +
                "\"Reason\":\"" + reason + "\"}"
                : "{\"CommandType\":\"" + BusCommands.Disconnect + "\"}";
      }
    });
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
}
