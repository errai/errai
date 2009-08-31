package org.jboss.workspace.server.security.auth;

import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.protocols.SecurityCommands;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import org.jboss.workspace.server.MessageBusServiceImpl;
import org.jboss.workspace.server.bus.MessageBusServer;
import org.jboss.workspace.server.bus.MessageListener;

import javax.servlet.http.HttpSession;


public class BasicAuthorizationListener implements MessageListener {
    private AuthorizationAdapter adapter;

    public BasicAuthorizationListener(AuthorizationAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean handleMessage(CommandMessage message) {
        if (adapter.requiresAuthorization(message)) {
            MessageBusServer.store("LoginClient",
                    CommandMessage.create(SecurityCommands.SecurityChallenge)
                            .set(SecurityParts.CredentialsRequired, "Name,Password")
                            .set(SecurityParts.ReplyTo, MessageBusServiceImpl.AUTHORIZATION_SVC_SUBJECT)
                            .set(SecurityParts.SessionData, message.get(HttpSession.class, SecurityParts.SessionData))
                    , false);

            return false;
        }
        return true;

    }
}
