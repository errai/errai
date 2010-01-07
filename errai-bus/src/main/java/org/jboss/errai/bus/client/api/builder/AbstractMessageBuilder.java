package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.ErrorCallback;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.RequestDispatcher;


public class AbstractMessageBuilder {
    private final Message message;

    public AbstractMessageBuilder(Message message) {
        this.message = message;
    }

    public MessageBuildSubject start() {
        final MessageBuildSendable sendable = new MessageBuildSendable() {
            public void sendNowWith(MessageBus viaThis) {
                message.sendNowWith(viaThis);
            }

            public void sendNowWith(MessageBus viaThis, boolean fireMessageListener) {
                viaThis.send(message, false);
            }

            public void sendNowWith(RequestDispatcher viaThis) {
                message.sendNowWith(viaThis);
            }

            public Message getMessage() {
                return message;
            }
        };

        final MessageBuildParms parmBuilder = new MessageBuildParms() {
            public MessageBuildParms with(String part, Object value) {
                message.set(part, value);
                return this;
            }

            public MessageBuildParms with(Enum part, Object value) {
                message.set(part, value);
                return this;
            }

            public MessageBuildParms copy(String part, Message m) {
                message.copy(part, m);
                return this;
            }

            public MessageBuildParms copy(Enum part, Message m) {
                message.copy(part, m);
                return this;
            }

            public MessageBuildParms copyResource(String part, Message m) {
                message.copyResource(part, m);
                return this;
            }

            public MessageBuildSendable errorsHandledBy(ErrorCallback callback) {
                message.errorsCall(callback);
                return sendable;

            }

            public MessageBuildSendable noErrorHandling() {
                return sendable;
            }

            public Message getMessage() {
                return message;
            }
        };

        final MessageBuildCommand command = new MessageBuildCommand() {
            public MessageBuildParms command(Enum command) {
                message.command(command);
                return parmBuilder;
            }

            public MessageBuildParms command(String command) {
                message.command(command);
                return parmBuilder;
            }

            public MessageBuildParms signalling() {
                return parmBuilder;
            }

            public Message getMessage() {
                return message;
            }
        };

        return new MessageBuildSubject() {
            public MessageBuildCommand toSubject(String subject) {
                message.toSubject(subject);
                return command;
            }

            public MessageBuildCommand subjectProvided() {
               return command;
            }

            public Message getMessage() {
                return message;
            }
        };
    }
}
