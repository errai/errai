package org.jboss.errai.server.security.auth;

import org.jboss.errai.client.bus.CommandMessage;
import org.jboss.errai.client.bus.protocols.SecurityCommands;
import org.jboss.errai.client.bus.protocols.SecurityParts;
import org.jboss.errai.server.bus.MessageBus;
import org.jboss.errai.server.bus.MessageListener;
import org.jboss.errai.server.bus.NoSubscribersToDeliverTo;
import org.jboss.errai.server.service.ErraiService;

import javax.servlet.http.HttpSession;


public class BasicAuthorizationListener implements MessageListener {
    private AuthorizationAdapter adapter;
    private MessageBus bus;

    public BasicAuthorizationListener(AuthorizationAdapter adapter, MessageBus bus) {
        this.adapter = adapter;
        this.bus = bus;
    }

    public boolean handleMessage(CommandMessage message) {
        if (adapter.requiresAuthorization(message)) {

            try {
                bus.send("LoginClient",
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
