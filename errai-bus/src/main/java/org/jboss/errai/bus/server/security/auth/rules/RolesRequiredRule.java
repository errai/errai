package org.jboss.errai.bus.server.security.auth.rules;

import org.jboss.errai.bus.client.BooleanRoutingRule;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.util.ServerBusUtils;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.service.ErraiService;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RolesRequiredRule implements BooleanRoutingRule {
    private Set<Object> requiredRoles;
    private MessageBus bus;

    public RolesRequiredRule(String[] requiredRoles, MessageBus bus) {
        this.requiredRoles = new HashSet<Object>();
        for (String role : requiredRoles) {
            this.requiredRoles.add(role.trim());
        }
        this.bus = bus;
    }

    public RolesRequiredRule(Set<Object> requiredRoles, MessageBus bus) {
        this.requiredRoles = requiredRoles;
        this.bus = bus;
    }

    public boolean decision(CommandMessage message) {
        if (!message.hasResource("Session")) return false;
        else {
            AuthSubject subject = (AuthSubject) getSession(message)
                    .getAttribute(ErraiService.SESSION_AUTH_DATA);

            if (subject == null) {
                /**
                 * Inform the client they must login.
                 */
                bus.send(CommandMessage.create(SecurityCommands.SecurityChallenge)
                        .toSubject("LoginClient")
                        .set(SecurityParts.CredentialsRequired, "Name,Password")
                        .set(SecurityParts.ReplyTo, ErraiService.AUTHORIZATION_SVC_SUBJECT)
                        .copyResource("Session", message)
                        .set(SecurityParts.RejectedMessage, ServerBusUtils.encodeJSON(message.getParts()))
                        , false);
                return false;
            }

            if (!subject.getRoles().containsAll(requiredRoles)) {
                ConversationMessage.create()
                        .toSubject("ClientErrorService")
                        .set(MessageParts.ErrorMessage, "Access denied to service: "
                                + message.get(String.class, MessageParts.ToSubject) +
                                " (Required Roles: [" + getRequiredRolesString() + "])")
                        .sendNowWith(bus);
                return false;

            } else {
                return true;
            }


        }
    }

    private String getRequiredRolesString() {
        StringBuilder builder = new StringBuilder();
        Iterator<Object> iter = requiredRoles.iterator();

        while (iter.hasNext()) {
            builder.append(String.valueOf(iter.next()));
            if (iter.hasNext()) builder.append(", ");
        }

        return builder.toString();
    }

    private static HttpSession getSession(CommandMessage message) {
        return ((HttpSession) message.getResource("Session"));
    }
}
