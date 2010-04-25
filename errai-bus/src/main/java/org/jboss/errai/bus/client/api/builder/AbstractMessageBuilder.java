package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.AsyncTask;
import org.jboss.errai.bus.client.api.base.ResourceProvider;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

import static org.jboss.errai.bus.client.api.base.ConversationHelper.createConversationService;
import static org.jboss.errai.bus.client.api.base.ConversationHelper.makeConversational;

/**
 * The <tt>AbstractMessageBuilder</tt> facilitates the building of a message, and ensures that it is created and used
 * properly.
 */
@SuppressWarnings({"unchecked"})
public class AbstractMessageBuilder<R extends Sendable> {
    private final Message message;

    public AbstractMessageBuilder(Message message) {
        this.message = message;
    }

    /**
     * Implements, creates and returns an instance of <tt>MessageBuildSubject</tt>. This is called initially when a
     * new message is created
     *
     * @return the <tt>MessageBuildSubject</tt> with the appropriate fields and functions for the message builder
     */
    public MessageBuildSubject start() {
        final Sendable sendable = new MessageReplySendable() {
            boolean reply = false;

            public MessageBuildSendable repliesTo(MessageCallback callback) {
                reply = true;
                makeConversational(message, callback);
                return this;
            }

            public void sendNowWith(MessageBus viaThis) {
                if (reply) createConversationService(viaThis, message);
                message.sendNowWith(viaThis);
            }

            public void sendNowWith(MessageBus viaThis, boolean fireMessageListener) {
                if (reply) createConversationService(viaThis, message);
                viaThis.send(message, false);
            }

            public void sendNowWith(RequestDispatcher viaThis) {
                message.sendNowWith(viaThis);
            }

            public void reply() {
                RequestDispatcher dispatcher = (RequestDispatcher)
                        ((ConversationMessageWrapper)message).getIncomingMessage()
                                .getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();

                if (dispatcher == null) {
                    throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
                }

                dispatcher.dispatch(message);
            }


            public AsyncTask sendRepeatingWith(RequestDispatcher viaThis, TimeUnit unit, int millis) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public AsyncTask sendDelayedWith(RequestDispatcher viaThis, TimeUnit unit, int millis) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Message getMessage() {
                return message;
            }
        };

        final MessageBuildParms<R> parmBuilder = new MessageBuildParms<R>() {
            public MessageBuildParms<R> with(String part, Object value) {
                message.set(part, value);
                return this;
            }

            public MessageBuildParms<R> with(Enum part, Object value) {
                message.set(part, value);
                return this;
            }

            public MessageBuildParms<R> copy(String part, Message m) {
                message.copy(part, m);
                return this;
            }

            public MessageBuildParms<R> copy(Enum part, Message m) {
                message.copy(part, m);
                return this;
            }

            public MessageBuildParms<R> copyResource(String part, Message m) {
                message.copyResource(part, m);
                return this;
            }

            public R errorsHandledBy(ErrorCallback callback) {
                message.errorsCall(callback);
                return (R) sendable;

            }

            public R noErrorHandling() {
                return (R) sendable;
            }

            public Message getMessage() {
                return message;
            }
        };

        final MessageBuildCommand<R> command = new MessageBuildCommand<R>() {
            public MessageBuildParms<R> command(Enum command) {
                message.command(command);
                return parmBuilder;
            }

            public MessageBuildParms<R> command(String command) {
                message.command(command);
                return parmBuilder;
            }

            public MessageBuildParms<R> signalling() {
                return parmBuilder;
            }

            public Message getMessage() {
                return message;
            }
        };

        return new MessageBuildSubject() {
            public MessageBuildCommand<R> toSubject(String subject) {
                message.toSubject(subject);
                return command;
            }

            public MessageBuildCommand<R> subjectProvided() {
                return command;
            }

            public Message getMessage() {
                return message;
            }
        };
    }
}
