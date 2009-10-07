package org.jboss.errai.bus.server.security.auth;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.bus.client.CommandMessage;


public class DefaultAdapter implements AuthenticationAdapter {
    public DefaultAdapter() {
        GWT.log("Warning: DefaultAdapter being used. This provides no security.", null);
    }

    public void challenge(CommandMessage message) {

    }

    public void process(CommandMessage message) {
    }

    public boolean endSession(CommandMessage message) {
        return false;
    }

    public boolean isAuthenticated(CommandMessage message) {
        return false;
    }

    public boolean requiresAuthorization(CommandMessage message) {
        return false;
    }
}
