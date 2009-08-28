package org.jboss.workspace.server.security.auth;

import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class JAASAdapter implements AuthorizationAdapter {
    private static final String AUTH_TOKEN = "WSAuthToken";

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

            LoginContext loginContext = new LoginContext(name, callbackHandler);

            loginContext.login();

            HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);
            session.setAttribute(AUTH_TOKEN, "AUTH");
        }
        catch (LoginException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    public boolean isAuthenticated(CommandMessage message) {
        HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);
        return session != null && AUTH_TOKEN.equals(session.getAttribute(AUTH_TOKEN));
    }
}
