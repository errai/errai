package org.jboss.errai.bus.client;

public class MessageBuilder {
    public static MessageBuildSubject create() {
        return new AbstractMessageBuilder(CommandMessage.create());
    }

    public static MessageBuildSubject createConversation(Message message) {
        return new AbstractMessageBuilder(ConversationMessage.create(message));
    }

    public static interface MessageBuildSubject {
        public MessageBuildCommand toSubject(String name);
    }

    public static interface MessageBuildCommand {
        public MessageBuildParms command(Enum command);

        public MessageBuildParms command(String command);

        public MessageBuildParms signals();
    }

    public static interface MessageBuildParms {
        public MessageBuildParms set(String part, Object value);

        public MessageBuildParms set(Enum part, Object value);

        public MessageBuildParms copy(String part, Message m);

        public MessageBuildParms copy(Enum part, Message m);

        public MessageBuildSendable errorHandledBy(ErrorCallback callback);

        public MessageBuildSendable noErrorHandling();
    }

    public static interface MessageBuildSendable {
        public void sendNowWith(MessageBus viaThis);

        public void sendNowWith(RequestDispatcher viaThis);
    }

    public static class AbstractMessageBuilder implements MessageBuildSubject {
        private final Message message;

        protected AbstractMessageBuilder(Message message) {
            this.message = message;
        }

        public MessageBuildCommand toSubject(final String subjectName) {
            message.toSubject(subjectName);

            final MessageBuildSendable sendable = new MessageBuildSendable() {
                public void sendNowWith(MessageBus viaThis) {
                    message.sendNowWith(viaThis);
                }

                public void sendNowWith(RequestDispatcher viaThis) {
                    message.sendNowWith(viaThis);
                }
            };

            final MessageBuildParms parmBuilder = new MessageBuildParms() {
                public MessageBuildParms set(String part, Object value) {
                    message.set(part, value);
                    return this;
                }

                public MessageBuildParms set(Enum part, Object value) {
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

                public MessageBuildSendable errorHandledBy(ErrorCallback callback) {
                    message.errorsCall(callback);
                    return sendable;

                }

                public MessageBuildSendable noErrorHandling() {
                    return sendable;
                }
            };

            return new MessageBuildCommand() {
                public MessageBuildParms command(Enum command) {
                    message.command(command);
                    return parmBuilder;
                }

                public MessageBuildParms command(String command) {
                    message.command(command);
                    return parmBuilder;
                }

                public MessageBuildParms signals() {
                    return parmBuilder;
                }
            };
        }
    }

}
