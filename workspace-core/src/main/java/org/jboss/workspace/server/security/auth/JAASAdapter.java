package org.jboss.workspace.server.security.auth;

import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import org.jboss.workspace.client.security.CredentialTypes;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class JAASAdapter implements AuthorizationAdapter {
    private static final String AUTH_TOKEN = "WSAuthToken";
    private HashMap<String, AuthDescriptor> securityRules = new HashMap<String, AuthDescriptor>();

    public JAASAdapter() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("login.config");

        if (url == null) throw new RuntimeException("cannot find login.config file");

        System.setProperty("java.security.auth.login.config", url.toString());
    }

    public void challenge(final CommandMessage message) {
        try {
            final String name = message.get(String.class, SecurityParts.Name);
            final String password = message.get(String.class, SecurityParts.Password);

            CallbackHandler callbackHandler = new CallbackHandler() {
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (Callback cb : callbacks) {
                        if (cb instanceof PasswordCallback) {
                            ((PasswordCallback) cb).setPassword(password.toCharArray());
                        }
                        else if (cb instanceof NameCallback) {
                            ((NameCallback) cb).setName(name);
                        }

                    }
                }
            };

            LoginContext loginContext = new LoginContext("Login", callbackHandler);

            loginContext.login();

            HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);
            session.setAttribute(AUTH_TOKEN, AUTH_TOKEN);

            System.out.println("*** Authentication Successful ***");
        }
        catch (LoginException e) {
            System.out.println("*** Authentication FAILED ***");
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAuthenticated(CommandMessage message) {
        HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);
        return session != null && AUTH_TOKEN.equals(session.getAttribute(AUTH_TOKEN));
    }

    public boolean requiresAuthorization(CommandMessage message) {
        String subject = message.getSubject();
        AuthDescriptor descriptor = securityRules.get(subject);     
        return descriptor != null && !descriptor.isAuthorized(message);
    }

    public void addSecurityRule(String subject, AuthDescriptor descriptor) {
        securityRules.put(subject, descriptor);
    }

    public void process(CommandMessage message) {
        if (isAuthenticated(message)) {
            getAuthDescriptor(message).add(new SimpleRole(CredentialTypes.Authenticated.name()));
        }
    }

    private Set getAuthDescriptor(CommandMessage message) {
        Set credentials = message.get(Set.class, SecurityParts.Credentials);
        if (credentials == null) {
            message.set(SecurityParts.Credentials, credentials = new HashSet());
        }
        return credentials;
    }

}
