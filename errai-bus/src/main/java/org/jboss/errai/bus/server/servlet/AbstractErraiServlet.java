/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;

/**
 * The <tt>AbstractErraiServlet</tt> provides a starting point for creating Http-protocol gateway between the server
 * bus and the client buses.
 */
public abstract class AbstractErraiServlet extends HttpServlet {
    /* New and configured errai service */
    protected ErraiService<HttpSession> service;

    /* A default Http session provider */
    protected SessionProvider<HttpSession> sessionProvider;

    protected volatile ClassLoader contextClassLoader;

    protected Logger log = LoggerFactory.getLogger(getClass());

    public enum ConnectionPhase {
        NORMAL, CONNECTING, DISCONNECTING, UNKNOWN
    }

    public static ConnectionPhase getConnectionPhase(HttpServletRequest request) {
        if (request.getHeader("phase") == null) return ConnectionPhase.NORMAL;
        else {
            String phase = request.getHeader("phase");
            if ("connection".equals(phase)) {
                return ConnectionPhase.CONNECTING;
            }
            else if ("disconnect".equals(phase)) {
                return ConnectionPhase.DISCONNECTING;
            }

            return ConnectionPhase.UNKNOWN;
        }
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        // Build or lookup service
        String jndiNameProperty = config.getInitParameter("jndiName");
        service = jndiNameProperty != null ?
                new JNDIServiceLocator(jndiNameProperty).locateService() : buildService();

        sessionProvider = service.getSessionProvider();

        contextClassLoader = Thread.currentThread().getContextClassLoader();

        service.getConfiguration().getResourceProviders()
                .put("errai.experimental.classLoader", new ResourceProvider<ClassLoader>() {
                    public ClassLoader get() {
                        return contextClassLoader;
                    }
                });
    }

    protected ErraiService<HttpSession> buildService() {
        return Guice.createInjector(new AbstractModule() {
            @SuppressWarnings({"unchecked"})
            public void configure() {
                bind(MessageBus.class).to(ServerMessageBusImpl.class);
                bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
                bind(new TypeLiteral<ErraiService<HttpSession>>() {
                }).to(new TypeLiteral<ErraiServiceImpl<HttpSession>>() {
                });

                bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
            }
        }).getInstance(new Key<ErraiService<HttpSession>>() {
        });
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
        } else {
            for (byte b : ((String) m.getMessage()).getBytes()) {
                stream.write(b);
            }
        }
        stream.write(']');

    }

    protected void sendDisconnectWithReason(OutputStream stream, final String reason) throws IOException {
        writeToOutputStream(stream, new MarshalledMessage() {
            public String getSubject() {
                return "ClientBus";
            }

            public Object getMessage() {
                return reason != null ? "{ToSubject:\"ClientBus\", CommandType:\"" + BusCommands.Disconnect + "\",Reason:\"" + reason + "\"}"
                        : "{CommandType:\"" + BusCommands.Disconnect + "\"}";
            }
        });
    }
}
