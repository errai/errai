package org.jboss.errai.server.security.auth;

import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.ConversationMessage;
import org.jboss.errai.client.rpc.protocols.MessageParts;
import org.jboss.errai.client.rpc.protocols.SecurityCommands;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.client.security.CredentialTypes;
import org.jboss.errai.server.bus.MessageBusServer;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A simple JAAS adapter to provide JAAS-based authentication.  This implementation currently defaults to a
 * property-file based authentication system and is still primarily for prototyping purposes.
 */
public class JAASAdapter implements AuthorizationAdapter {
    /**
     * A simple token to add to a session to indicate successful authorization.
     */
    private static final String AUTH_TOKEN = "WSAuthToken";

    /**
     * A Map to hold a list of message subjects and their AuthDescriptors.  This isn't using JAAS-ey based
     * authorization at this point, but rather a Workspace-centric abstraction API.  But that can be fixed
     * later.
     */
    private HashMap<String, AuthDescriptor> securityRules = new HashMap<String, AuthDescriptor>();

    public JAASAdapter() {
        /**
         * Try and find the default login.config file.
         */
        URL url = Thread.currentThread().getContextClassLoader().getResource("login.config");
        if (url == null) throw new RuntimeException("cannot find login.config file");

        /**
         * Override the JAAS configuration to point to our login config. Yes, this is really bad, and
         * is for demonstration purposes only.  This will need to be removed at a later point.
         */
        System.setProperty("java.security.auth.login.config", url.toString());
    }

    /**
     * Send a challenge to the authentication system.
     *
     * @param message
     */
    public void challenge(final CommandMessage message) {
        final String name = message.get(String.class, SecurityParts.Name);
        final String password = message.get(String.class, SecurityParts.Password);
        try {
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

            /**
             * Load the default Login context.
             */
            LoginContext loginContext = new LoginContext("Login", callbackHandler);

            /**
             * Attempt to login.
             */
            loginContext.login();


            /**
             * If we got this far, then the authentication succeeded. So grab access to the HTTPSession and
             * add the authorization token.
             */
            addAuthenticationToken(message);

            /**
             * Prepare to send a message back to the client, informing it that a successful login has
             * been performed.
             */
            ConversationMessage successfulMsg = ConversationMessage.create(SecurityCommands.SuccessfulAuth, message)
                    .set(SecurityParts.Name, name);

            try {
                ResourceBundle bundle = ResourceBundle.getBundle("errai");
                String motdText = bundle.getString("workspace.login_motd");

                /**
                 * If the MOTD is configured, then add it to the message.
                 */
                if (motdText != null) {
                    successfulMsg.set(MessageParts.MessageText, motdText);
                }
            }
            catch (Exception e) {
                // do nothing.
            }

            /**
             * Transmit the message back to the client.
             */
            MessageBusServer.store("LoginClient", successfulMsg);
        }
        catch (LoginException e) {
            /**
             * The login failed. How upsetting. Life must go on, and we must inform the client of the
             * unfortunate news.
             */
            MessageBusServer.store("LoginClient", ConversationMessage.create(SecurityCommands.FailedAuth, message)
                    .set(SecurityParts.Name, name));

            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAuthenticationToken(CommandMessage message) {
        HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);
        session.setAttribute(AUTH_TOKEN, AUTH_TOKEN);
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
