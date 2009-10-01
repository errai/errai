package org.jboss.errai.server.security.auth;

import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.protocols.SecurityCommands;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.server.MessageBusServiceImpl;
import org.jboss.errai.server.bus.MessageBusServer;
import org.jboss.errai.server.bus.MessageListener;
import org.jboss.errai.server.bus.NoSubscribersToDeliverTo;
import org.jboss.errai.server.service.ErraiService;
import org.jboss.errai.server.service.ErraiServiceImpl;

import javax.servlet.http.HttpSession;


public class BasicAuthorizationListener implements MessageListener {
    private AuthorizationAdapter adapter;

    public BasicAuthorizationListener(AuthorizationAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean handleMessage(CommandMessage message) {
        if (adapter.requiresAuthorization(message)) {

            try {
                MessageBusServer.send("LoginClient",
                        CommandMessage.create(SecurityCommands.SecurityChallenge)
                                .set(SecurityParts.CredentialsRequired, "Name,Password")
                                .set(SecurityParts.ReplyTo, ErraiService.AUTHORIZATION_SVC_SUBJECT)
                                .set(SecurityParts.SessionData, message.get(HttpSession.class, SecurityParts.SessionData))
                        , false);

                return false;
            }
            catch (NoSubscribersToDeliverTo e) {
                System.out.println("**NO LOGINCLIENT*");
            }
        }
        return true;

    }
}
