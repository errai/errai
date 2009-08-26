package org.jboss.workspace.server.security.auth;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;

public class JAASAdapter implements AuthorizationAdapter {
    public void challenge(final String name, final String password) {
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

            LoginContext loginContext = new LoginContext(name, callbackHandler);
            
            loginContext.login();
        }
        catch (LoginException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }
}
