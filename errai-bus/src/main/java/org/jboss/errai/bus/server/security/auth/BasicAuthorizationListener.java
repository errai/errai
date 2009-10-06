package org.jboss.errai.bus.server.security.auth;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageListener;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.server.service.ErraiService;

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
                bus.send(CommandMessage.create(SecurityCommands.SecurityChallenge)
                                .toSubject("LoginClient")
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
