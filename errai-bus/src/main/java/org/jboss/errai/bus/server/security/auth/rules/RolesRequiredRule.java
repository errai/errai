/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.security.auth.rules;

import org.jboss.errai.bus.client.framework.BooleanRoutingRule;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.bus.server.util.ServerBusUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;

/**
 * This routing rule specifies a set of required roles that a message must posess in order for this routing rule
 * to return true.
 */
public class RolesRequiredRule implements BooleanRoutingRule {
    private Set<Object> requiredRoles;
    private ServerMessageBus bus;

    public RolesRequiredRule(String[] requiredRoles, ServerMessageBus bus) {
        this.requiredRoles = new HashSet<Object>();
        for (String role : requiredRoles) {
            this.requiredRoles.add(role.trim());
        }
        this.bus = bus;
    }

    public RolesRequiredRule(Set<Object> requiredRoles, ServerMessageBus bus) {
        this.requiredRoles = requiredRoles;
        this.bus = bus;
    }

    public boolean decision(final Message message) {
        if (!message.hasResource("Session")) return false;
        else {

            AuthSubject subject = getSession(message).getAttribute(AuthSubject.class, ErraiService.SESSION_AUTH_DATA);

            if (subject == null) {
                /**
                 * Inform the client they must login.
                 */

                if ("LoginClient".equals(message.getSubject())) {
                    /**
                     * Make an exception for the LoginClient ...
                     */
                    return true;
                }


                // TODO: This reside with the "AuthenticationService" listener, no
                // i.e. by forwarding to that subject. See ErraiServiceImpl
                createMessage()
                        .toSubject("LoginClient")
                        .command(SecurityCommands.SecurityChallenge)
                        .with(SecurityParts.CredentialsRequired, "Name,Password")
                        .with(MessageParts.ReplyTo, ErraiService.AUTHORIZATION_SVC_SUBJECT)
                        .with(SecurityParts.RejectedMessage, ServerBusUtils.encodeJSON(message.getParts()))
                        .copyResource("Session", message)
                        .errorsHandledBy(new ErrorCallback() {
                            public boolean error(Message message, Throwable throwable) {
                                ErrorHelper.sendClientError(bus, message, throwable.getMessage(), throwable);
                                return false;
                            }
                        })
                        .sendNowWith(bus, false);

                return false;
            }

            if (!subject.getRoles().containsAll(requiredRoles)) {
                createConversation(message)
                        .toSubject("ClientErrorService")
                        .with(MessageParts.ErrorMessage, "Access denied to service: "
                                + message.get(String.class, MessageParts.ToSubject) +
                                " (Required Roles: [" + getRequiredRolesString() + "])")
                        .noErrorHandling().sendNowWith(bus);

                return false;

            } else {
                return true;
            }
        }
    }

    public String getRequiredRolesString() {
        StringBuilder builder = new StringBuilder();
        Iterator<Object> iter = requiredRoles.iterator();

        while (iter.hasNext()) {
            builder.append(String.valueOf(iter.next()));
            if (iter.hasNext()) builder.append(", ");
        }

        return builder.toString();
    }

    public Set<Object> getRoles() {
        return requiredRoles;
    }

    private static QueueSession getSession(Message message) {
        return message.getResource(QueueSession.class, "Session");
    }
}
