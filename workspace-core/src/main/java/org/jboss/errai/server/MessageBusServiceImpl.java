package org.jboss.errai.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.ConversationMessage;
import org.jboss.errai.client.rpc.MessageBusService;
import org.jboss.errai.client.rpc.protocols.MessageParts;
import org.jboss.errai.client.rpc.protocols.SecurityCommands;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.client.security.CredentialTypes;
import org.jboss.errai.server.annotations.LoadModule;
import org.jboss.errai.server.bus.DefaultMessageBusProvider;
import org.jboss.errai.server.bus.Message;
import org.jboss.errai.server.bus.MessageBus;
import org.jboss.errai.server.bus.MessageBusServer;
import org.jboss.errai.server.json.JSONUtil;
import org.jboss.errai.server.security.auth.AuthorizationAdapter;
import org.jboss.errai.server.security.auth.BasicAuthorizationListener;
import org.jboss.errai.server.security.auth.JAASAdapter;
import org.jboss.errai.server.security.auth.RoleAuthDescriptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import static java.lang.Thread.currentThread;
import java.net.URL;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The main gateway of the Workspace application to the server.  All communication between the client and the
 * server passes through this class.
 */
public class MessageBusServiceImpl extends RemoteServiceServlet implements MessageBusService {
    private MessageBus bus;
    private AuthorizationAdapter authorizationAdapter;


    public static final String AUTHORIZATION_SVC_SUBJECT = "AuthorizationService";

    @Override
    public void init() throws ServletException {
        // just use the simple bus for now.  more integration options to come...
        bus = new DefaultMessageBusProvider().getBus();

        // initialize the configuration.
        loadConfig();

        bus.addGlobalListener(new BasicAuthorizationListener(authorizationAdapter));

        //todo: this all needs to be refactored at some point.
        bus.subscribe(AUTHORIZATION_SVC_SUBJECT, new MessageCallback() {
            public void callback(CommandMessage c) {
                switch (SecurityCommands.valueOf(c.getCommandType())) {
                    case WhatCredentials:
                        /**
                         * Respond with what credentials the authentication system requires.
                         */
                        //todo: we only support login/password for now
                        MessageBusServer.store(c.get(String.class, SecurityParts.ReplyTo),
                                ConversationMessage.create(SecurityCommands.WhatCredentials, c)
                                        .set(SecurityParts.CredentialsRequired, "Name,Password")
                                        .set(SecurityParts.ReplyTo, AUTHORIZATION_SVC_SUBJECT));
                        break;

                    case AuthRequest:
                        /**
                         * Send a challenge.
                         */
                        authorizationAdapter.challenge(c);
                        break;

                    case EndSession:
                        authorizationAdapter.endSession(c);
                        MessageBusServer.store(ConversationMessage.create(c).setSubject("LoginClient")
                                .setCommandType(SecurityCommands.SecurityChallenge));
                        break;
                }
            }
        });

        /**
         * A temporary test service to bounce remote messages of.
         */
        bus.subscribe("TestService", new MessageCallback() {
            public void callback(CommandMessage message) {
                System.out.println("yay!");
            }
        });

        /**
         * Some temporary security rules to test the login system.
         */
        RoleAuthDescriptor authRequired = new RoleAuthDescriptor(new String[]{CredentialTypes.Authenticated.name()});
        ((JAASAdapter) authorizationAdapter).addSecurityRule("TestService", authRequired);
        ((JAASAdapter) authorizationAdapter).addSecurityRule("ServerEchoService", authRequired);

        /**
         * The standard ServerEchoService.
         */
        bus.subscribe("ServerEchoService", new MessageCallback() {
            public void callback(CommandMessage c) {
                if (c.hasPart(MessageParts.ReplyTo)) {
                    MessageBusServer.store(ConversationMessage.create(c));
                }
            }
        });


    }

    private void loadConfig() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("errai");
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

        try {
            ResourceBundle bundle = ResourceBundle.getBundle("errai");
            String modulesToLoad = bundle.getString("workspace.server_modules");

            String[] moduleFQCN = modulesToLoad.split(",");
            Class<Module>[] moduleClass = new Class[moduleFQCN.length];

            try {
                for (int i = 0; i < moduleFQCN.length; i++) {
                    if (moduleClass[i] == null) continue;
                    try {
                        moduleClass[i] = (Class<Module>) Class.forName(moduleFQCN[i]);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("unable to load module: " + moduleClass[i], e);
                    }
                }
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException("error", e);
            }

            try {
                for (Class<Module> clazz : moduleClass) {
                    if (clazz == null) continue;
                    clazz.newInstance().init();
                }
            }
            catch (Exception e) {
                throw new RuntimeException("error loading module: " + e.getMessage(), e);
            }


            try {
                Enumeration<URL> targets = currentThread().getContextClassLoader().getResources("ErraiApp.properties");

                while (targets.hasMoreElements()) {
                    findLoadableModules(targets.nextElement());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        catch (Exception e) {
            throw new RuntimeException("unable to load config", e);
        }
    }

    private void findLoadableModules(URL url) {
        File root = new File(url.getFile()).getParentFile();
        _findLoadableModules(root, root);
    }

    private void _findLoadableModules(File root, File start) {
        for (File file : start.listFiles()) {
            if (file.isDirectory()) _findLoadableModules(root, file);
            if (file.getName().endsWith(".class")) {
                try {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());
                    Class clazz = Class.forName(FQCN);
                    if (clazz.isAnnotationPresent(LoadModule.class)) {
                        ((Module) clazz.newInstance()).init();
                    }
                }
                catch (NoClassDefFoundError e) {
                    // do nothing.
                }
                catch (ExceptionInInitializerError e) {
                    // do nothing.
                }
                catch (UnsupportedOperationException e) {
                    // do nothing.
                }
                catch (ClassNotFoundException e) {
                    // do nothing.
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to load module", e);
                }
                catch (InstantiationException e) {
                    throw new RuntimeException("Failed to load module", e);
                }
            }
        }
    }

    private String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }


    private void useDefaults() {
        authorizationAdapter = new JAASAdapter();
    }

    /**
     * Store a new message onto the bus.
     *
     * @param subject -
     * @param message -
     */
    public void store(String subject, String message) {
        //System.out.println("RecvMsgFromClient (Subject:" + subject + ";Message=" + message + ")");

        System.out.println("INCOMING_MSG (@" + subject + "):" + message);

        CommandMessage translatedMessage = new CommandMessage();

        /**
         * Assuming we didn't receive a null payload (which is possible, as you can send subject-based signalling),
         * we will decode the payload back into workable form.
         */
        if (message != null) {
            translatedMessage.setParts(JSONUtil.decodeToMap(message));
        }

        /**
         * Add the current HTTPSession to the payload for access and use by services on the server.
         */
        translatedMessage.set(SecurityParts.SessionData, getSession()).setSubject(subject);


        /**
         * If an authorization adapter is configured, we now allow it to pre-process the request.
         */
        if (authorizationAdapter != null) {
            authorizationAdapter.process(translatedMessage);
        }

        /**
         * Pass the message off to the messaging bus for handling.
         */
        try {
            bus.storeGlobal(subject, translatedMessage);
        }
        catch (Throwable t) {
            System.err.println("Message was not delivered.");
            t.printStackTrace();
        }
    }

    /**
     * Retrieve the next waiting message from the bus.
     *
     * @return
     */
    public String[] nextMessage() {
        Message m = bus.nextMessage(getId());
        if (m != null) {
            return new String[]{m.getSubject(), String.valueOf(m.getMessage())};
        }
        else {
            return null;
        }
    }

    private HttpSession getSession() {
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();

        if (session.getAttribute(MessageBus.WS_SESSION_ID) == null) {
            session.setAttribute(MessageBus.WS_SESSION_ID, session.getId());
        }

        return session;
    }

    /**
     * The the unique session identifier.
     *
     * @return
     */
    private String getId() {
        return (String) getSession().getAttribute(MessageBus.WS_SESSION_ID);
    }

    /**
     * Return a list of all available subjects in the server bus.
     *
     * @return
     */
    public String[] getSubjects() {
        return bus.getSubjects().toArray(new String[bus.getSubjects().size()]);
    }
}
