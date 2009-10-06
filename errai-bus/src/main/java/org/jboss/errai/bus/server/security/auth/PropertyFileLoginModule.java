package org.jboss.errai.bus.server.security.auth;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;
import java.util.ResourceBundle;

public class PropertyFileLoginModule implements LoginModule {
    private String login;
    private String password;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        NameCallback name = new NameCallback("Login");
        PasswordCallback password = new PasswordCallback("Password", false);

        try {
            callbackHandler.handle(new Callback[]{name, password});
            this.login = name.getName();
            this.password = new String(password.getPassword());

        }
        catch (Exception e) {
            throw new RuntimeException("could not login", e);
        }
    }

    public boolean login() throws LoginException {
        ResourceBundle bundle = ResourceBundle.getBundle("users");
        String password = bundle.getString(login);
        return password != null && this.password.equals(password);
    }

    public boolean commit() throws LoginException {
        return true;
    }

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean logout() throws LoginException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
