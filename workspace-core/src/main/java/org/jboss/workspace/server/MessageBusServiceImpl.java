package org.jboss.workspace.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.workspace.client.framework.MessageCallback;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.MessageBusService;
import org.jboss.workspace.client.rpc.protocols.SecurityCommands;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import org.jboss.workspace.client.security.CredentialTypes;
import org.jboss.workspace.server.bus.*;
import org.jboss.workspace.server.json.JSONUtil;
import org.jboss.workspace.server.security.auth.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import static java.lang.Thread.currentThread;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageBusServiceImpl extends RemoteServiceServlet implements MessageBusService {
    private MessageBus bus;
    private AuthorizationAdapter authorizationAdapter;

    public static final String WS_SESSION_ID = "WSSessionID";
    public static final String AUTHORIZATION_SVC_SUBJECT = "AuthorizationService";

    @Override
    public void init() throws ServletException {
        // just use the simple bus for now.  more integration options to come...
        bus = new SimpleMessageBusProvider().getBus();

        loadConfig();

        bus.addGlobalListener(new BasicAuthorizationListener(authorizationAdapter));

        //todo: this all needs to be refactored at some point.
        bus.subscribe(AUTHORIZATION_SVC_SUBJECT, new MessageCallback() {
            public void callback(CommandMessage c) {
                switch (SecurityCommands.valueOf(c.getCommandType())) {
                    case WhatCredentials:
                        //todo: we only support login/password for now
                        CommandMessage reply = new CommandMessage(SecurityCommands.WhatCredentials)
                                .set(SecurityParts.CredentialsRequired, "Name,Password")
                                .set(SecurityParts.ReplyTo, AUTHORIZATION_SVC_SUBJECT);

                        MessageBusServer.store(c.get(String.class, SecurityParts.ReplyTo), reply);
                }

            }
        });

        bus.subscribe("TestService", new MessageCallback() {
            public void callback(CommandMessage message) {
                System.out.println("yay!");
            }
        });

        ((JAASAdapter) authorizationAdapter).addSecurityRule("TestService", new RoleAuthDescriptor(new String[] { CredentialTypes.Authenticated.name() }));

        bus.subscribe("ServerEchoService", new MessageCallback() {
            public void callback(CommandMessage c) {


                if (c.hasPart("EchoBackData")) {
                    System.out.println("EchoBack: " + c.get(String.class, "EchoBackData"));
                }
                else {
                    System.out.println("Echo!");
                }
            }
        });


        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 5);

                    CommandMessage msg = new CommandMessage("Hello");
                    msg.set("Name", "Jay Balunas");

                    bus.storeGlobal("org.jboss.workspace.WorkspaceLayout", msg, false);

                }
                catch (InterruptedException e) {
                }

            }
        };

        t.start();
    }

    private void loadConfig() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("workspace");
            String authenticationAdapterClass = bundle.getString("workspace.authentication_adapter");

            try {
                Class clazz = Class.forName(authenticationAdapterClass, false, currentThread().getContextClassLoader());
                authorizationAdapter = (AuthorizationAdapter) clazz.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("could not instantiate authentication adapter:" + authenticationAdapterClass, e);
            }
        }
        catch (MissingResourceException e) {
            useDefaults();
        }
    }

    private void useDefaults() {
        authorizationAdapter = new JAASAdapter();
    }

    public void store(String subject, String message) {
        System.out.println("MessageRecvFromClient <<" + subject + ":" + message + ">>");

        CommandMessage translatedMessage = new CommandMessage();
        if (message != null) {
            translatedMessage.setParts(JSONUtil.decodeToMap(message));
        }
        translatedMessage.set(SecurityParts.SessionData, getSession()).setSubject(subject);

        if (authorizationAdapter != null) authorizationAdapter.process(translatedMessage);

        bus.storeGlobal(subject, translatedMessage);
    }

    public String[] nextMessage() {
        Message m = bus.nextMessage(getId());
        if (m != null) {
            return new String[]{m.getSubject(), String.valueOf(m.getMessage())};
        }
        else {
            return null;
        }
    }

    public void remoteSubscribe(String subject) {
        if (bus.getSubjects().contains(subject)) return;
        bus.remoteSubscribe(getId(), subject);
    }

    private HttpSession getSession() {
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();

        if (session.getAttribute(WS_SESSION_ID) == null) {
            session.setAttribute(WS_SESSION_ID, session.getId());
        }

        return session;
    }

    private String getId() {
        return (String) getSession().getAttribute(WS_SESSION_ID);
    }

    public String[] getSubjects() {
        return bus.getSubjects().toArray(new String[bus.getSubjects().size()]);
    }
}
