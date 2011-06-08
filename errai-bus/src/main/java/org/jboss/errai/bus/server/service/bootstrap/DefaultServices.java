/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.bus.server.service.bootstrap;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.security.auth.AuthSubject;
import org.jboss.errai.bus.server.security.auth.AuthenticationAdapter;
import org.jboss.errai.bus.server.security.auth.AuthenticationFailedException;
import org.jboss.errai.bus.server.service.ErraiService;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

/**
 * Setup the default services.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
class DefaultServices implements BootstrapExecution {

  public void execute(final BootstrapContext context) {
    final ServerMessageBus bus = context.getBus();
    final boolean authenticationConfigured =
        context.getConfig().getResource(AuthenticationAdapter.class) != null;

    bus.subscribe(ErraiService.AUTHORIZATION_SVC_SUBJECT, new MessageCallback() {
      public void callback(Message message) {
        switch (SecurityCommands.valueOf(message.getCommandType())) {
          case AuthenticationScheme:
            if (authenticationConfigured) {

              /**
               * Respond with what credentials the authentication system requires.
               */
              //todo: we only support login/password for now

              createConversation(message)
                  .subjectProvided()
                  .command(SecurityCommands.AuthenticationScheme)
                  .with(SecurityParts.CredentialsRequired, "Name,Password")
                  .with(MessageParts.ReplyTo, ErraiService.AUTHORIZATION_SVC_SUBJECT)
                  .noErrorHandling().sendNowWith(bus);
            }
            else {
              createConversation(message)
                  .subjectProvided()
                  .command(SecurityCommands.AuthenticationNotRequired)
                  .noErrorHandling().sendNowWith(bus);
            }

            break;

          case AuthRequest:
            /**
             * Receive a challenge.
             */

            if (authenticationConfigured) {
              try {
                context.getConfig().getResource(AuthenticationAdapter.class)
                    .challenge(message);
              }
              catch (AuthenticationFailedException a) {
              }
            }
            break;

          case EndSession:
            if (authenticationConfigured) {
              context.getConfig().getResource(AuthenticationAdapter.class)
                  .endSession(message);
            }

            // reply in any case
            createConversation(message)
                .toSubject("LoginClient")
                .command(SecurityCommands.EndSession)
                .noErrorHandling()
                .sendNowWith(bus);

            break;
        }
      }
    });

    /**
     * The standard ServerEchoService.
     */
    bus.subscribe(ErraiService.SERVER_ECHO_SERVICE, new MessageCallback() {
      public void callback(Message c) {
        MessageBuilder.createConversation(c)
            .subjectProvided().noErrorHandling()
            .sendNowWith(bus);
      }
    });

    bus.subscribe(ErraiService.AUTHORIZATION_SERVICE, new MessageCallback() {
      public void callback(Message message) {
        AuthSubject subject = message.getResource(QueueSession.class, "Session")
            .getAttribute(AuthSubject.class, ErraiService.SESSION_AUTH_DATA);

        Message reply = MessageBuilder.createConversation(message).getMessage();

        if (subject != null) {
          reply.set(SecurityParts.Roles, subject.toRolesString());
          reply.set(SecurityParts.Name, subject.getUsername());
        }

        reply.sendNowWith(bus);
      }
    });
  }
}
