package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.api.base.*;
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

            public void sendGlobalWith(MessageBus viaThis) {
                viaThis.sendGlobal(message);
            }

            public void sendGlobalWith(RequestDispatcher viaThis) {
                viaThis.dispatchGlobal(message);
            }

            public void reply() {
                RequestDispatcher dispatcher = (RequestDispatcher)
                        getIncomingMessage().getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();

                if (dispatcher == null) {
                    throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
                }

                dispatcher.dispatch(message);
            }

            public AsyncTask replyRepeating(TimeUnit unit, int interval) {
                Message msg = getIncomingMessage();
                message.copyResource("Session", msg);
                RequestDispatcher dispatcher = (RequestDispatcher) msg.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();
                return _sendRepeatingWith(message, dispatcher, unit, interval);
            }

            public AsyncTask replyDelayed(TimeUnit unit, int interval) {
                Message msg = getIncomingMessage();
                message.copyResource("Session", msg);
                RequestDispatcher dispatcher = (RequestDispatcher) msg.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();
                return _sendDelayedWith(message, dispatcher, unit, interval);
            }

            private Message getIncomingMessage() {
                return ((ConversationMessageWrapper) message).getIncomingMessage();
            }

            public AsyncTask sendRepeatingWith(final RequestDispatcher viaThis, TimeUnit unit, int interval) {
                return _sendRepeatingWith(null, viaThis, unit, interval);
            }

            public AsyncTask sendDelayedWith(final RequestDispatcher viaThis, TimeUnit unit, int interval) {
                return _sendDelayedWith(null, viaThis, unit, interval);
            }

            private AsyncTask _sendRepeatingWith(final Message message, final RequestDispatcher viaThis, TimeUnit unit, int interval) {
                return TaskManagerFactory.get().scheduleRepeating(unit, interval, new HasAsyncTaskRef() {
                    AsyncTask task;
                    AsyncDelegateErrorCallback errorCallback
                            = new AsyncDelegateErrorCallback(this, message.getErrorCallback());

                    public void setAsyncTask(AsyncTask task) {
                        synchronized (this) {
                            this.task = task;
                        }
                    }


                    public AsyncTask getAsyncTask() {
                        synchronized (this) {
                            return task;
                        }
                    }

                    public void run() {
                        MessageBuilder.getMessageProvider().get()
                                .copyResource("Session", message)
                                .addAllParts(message.getParts())
                                .addAllProvidedParts(message.getProvidedParts())
                                .errorsCall(errorCallback).sendNowWith(viaThis);
                    }
                });
            }


            public AsyncTask _sendDelayedWith(final Message message, final RequestDispatcher viaThis, TimeUnit unit, int interval) {
                return TaskManagerFactory.get().schedule(unit, interval, new HasAsyncTaskRef() {
                    AsyncTask task;
                    AsyncDelegateErrorCallback errorCallback
                            = new AsyncDelegateErrorCallback(this, message.getErrorCallback());

                    public void setAsyncTask(AsyncTask task) {
                        synchronized (this) {
                            this.task = task;
                        }
                    }


                    public AsyncTask getAsyncTask() {
                        synchronized (this) {
                            return task;
                        }
                    }

                    public void run() {
                        MessageBuilder.getMessageProvider().get()
                                .copyResource("Session", message)
                                .addAllParts(message.getParts())
                                .addAllProvidedParts(message.getProvidedParts())
                                .errorsCall(errorCallback).sendNowWith(viaThis);
                    }
                });
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

            public MessageBuildParms<R> withProvided(String part, ResourceProvider provider) {
                message.setProvidedPart(part, provider);
                return this;
            }

            public MessageBuildParms<R> withProvided(Enum part, ResourceProvider provider) {
                message.setProvidedPart(part, provider);
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

            public R defaultErrorHandling() {
                message.errorsCall(DefaultErrorCallback.INSTANCE);
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
