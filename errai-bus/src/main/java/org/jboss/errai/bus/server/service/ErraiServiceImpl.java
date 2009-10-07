package org.jboss.errai.bus.server.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.Module;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.annotations.LoadModule;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import static java.lang.Thread.currentThread;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class ErraiServiceImpl implements ErraiService {
    private ServerMessageBus bus;
    private AuthenticationAdapter authenticationAdapter;


    @Inject
    public ErraiServiceImpl(ServerMessageBus bus, AuthenticationAdapter authenticationAdapter) {
        this.bus = bus;
        this.authenticationAdapter = authenticationAdapter;

        init();
    }

    private void init() {
        // just use the simple bus for now.  more integration options sendNowWith come...
        //bus.addGlobalListener(new BasicAuthorizationListener(authorizationAdapter, bus));

        //todo: this all needs sendNowWith be refactored at some point.
        bus.subscribe(AUTHORIZATION_SVC_SUBJECT, new MessageCallback() {
            public void callback(CommandMessage c) {
                switch (SecurityCommands.valueOf(c.getCommandType())) {
                    case WhatCredentials:
                        /**
                         * Respond with what credentials the authentication system requires.
                         */
                        //todo: we only support login/password for now
                        bus.send(c.get(String.class, SecurityParts.ReplyTo),
                                ConversationMessage.create(SecurityCommands.WhatCredentials, c)
                                        .set(SecurityParts.CredentialsRequired, "Name,Password")
                                        .set(SecurityParts.ReplyTo, AUTHORIZATION_SVC_SUBJECT));
                        break;

                    case AuthRequest:
                        /**
                         * Send a challenge.
                         */
                        authenticationAdapter.challenge(c);
                        break;

                    case EndSession:
                        authenticationAdapter.endSession(c);
                        bus.send(ConversationMessage.create(c).toSubject("LoginClient")
                                .setCommandType(SecurityCommands.SecurityChallenge));
                        break;
                }
            }
        });


        /**
         * The standard ServerEchoService.
         */
        bus.subscribe("ServerEchoService", new MessageCallback() {
            public void callback(CommandMessage c) {
                bus.send(ConversationMessage.create(c));
            }
        });

        bus.subscribe("ClientNegotiationService", new MessageCallback() {
            public void callback(CommandMessage message) {
                AuthSubject subject = (AuthSubject)
                        message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(ErraiService.SESSION_AUTH_DATA);

                ConversationMessage reply = ConversationMessage.create(message);

                if (subject != null) {
                    reply.set(SecurityParts.Roles, subject.toRolesString());
                    reply.set(SecurityParts.Name, subject.getUsername());
                }

                reply.sendNowWith(bus);
            }
        });

        loadConfig();
    }

    public void store(CommandMessage message) {
        /**
         * Pass the message off to the messaging bus for handling.
         */
        try {
            bus.sendGlobal(message);
        }
        catch (Throwable t) {
            System.err.println("Message was not delivered.");
            t.printStackTrace();
        }
    }

    private void loadConfig() {

        try {
            try {
                Enumeration<URL> targets = currentThread().getContextClassLoader().getResources("ErraiApp.properties");
                Set<String> loaded = new HashSet<String>();

                while (targets.hasMoreElements()) {
                    findLoadableModules(targets.nextElement(), loaded);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("unable to load config", e);
        }

        try {
            ResourceBundle erraiServiceConfig = ResourceBundle.getBundle("ErraiService");

            Enumeration<String> keys = erraiServiceConfig.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();

                if ("errai.require_authentication_for_all".equals(key)) {
                    if("true".equals(erraiServiceConfig.getString(key))) {
                        bus.addRule("ClientNegotiationService", new RolesRequiredRule(new HashSet<Object>(), bus));
                    }
                }

            }
        }
        catch (Exception e) {
            throw new RuntimeException("error reading from configuration", e);
        }
    }

    private void findLoadableModules(URL url, Set<String> loaded) {
        File root = new File(url.getFile()).getParentFile();
        _findLoadableModules(root, root, loaded);
    }

    private void _findLoadableModules(File root, File start, Set<String> loaded) {
        for (File file : start.listFiles()) {
            if (file.isDirectory()) _findLoadableModules(root, file, loaded);
            if (file.getName().endsWith(".class")) {
                try {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());

                    if (loaded.contains(FQCN)) {
                        return;
                    }
                    else {
                        loaded.add(FQCN);
                    }

                    Class<?> loadClass = Class.forName(FQCN);

                    if (Module.class.isAssignableFrom(loadClass)) {
                        final Class<? extends Module> clazz = loadClass.asSubclass(Module.class);

                        if (clazz.isAnnotationPresent(LoadModule.class)) {
                            Guice.createInjector(new AbstractModule() {
                                @Override
                                protected void configure() {
                                    bind(Module.class).to(clazz);
                                    bind(MessageBus.class).toInstance(bus);
                                }
                            }).getInstance(Module.class).init();
                        }

                    }
                    else if (MessageCallback.class.isAssignableFrom(loadClass)) {
                        final Class<? extends MessageCallback> clazz = loadClass.asSubclass(MessageCallback.class);
                        if (clazz.isAnnotationPresent(Service.class)) {
                            MessageCallback svc = Guice.createInjector(new AbstractModule() {
                                @Override
                                protected void configure() {
                                    bind(MessageCallback.class).to(clazz);
                                    bind(MessageBus.class).toInstance(bus);
                                }
                            }).getInstance(MessageCallback.class);

                            String svcName = clazz.getAnnotation(Service.class).value();

                            bus.subscribe(svcName, svc);

                            RolesRequiredRule rule = null;
                            if (clazz.isAnnotationPresent(RequireRoles.class)) {
                                rule = new RolesRequiredRule(clazz.getAnnotation(RequireRoles.class).value(), bus);
                            }
                            else if (clazz.isAnnotationPresent(RequireAuthentication.class)) {
                                rule = new RolesRequiredRule(new HashSet<Object>(), bus);
                            }
                            if (rule != null) {
                                bus.addRule(svcName, rule);
                            }
                        }

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
                catch (UnsatisfiedLinkError e) {
                    // do nothing.
                }
            }
        }
    }

    private String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }


    public ServerMessageBus getBus() {
        return bus;
    }
}
