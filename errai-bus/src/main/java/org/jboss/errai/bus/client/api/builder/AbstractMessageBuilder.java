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

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.api.base.*;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;

import static org.jboss.errai.bus.client.api.base.ConversationHelper.createConversationService;
import static org.jboss.errai.bus.client.api.base.ConversationHelper.makeConversational;

/**
 * The <tt>AbstractMessageBuilder</tt> facilitates the building of a message,
 * and ensures that it is created and used properly.
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class AbstractMessageBuilder<R extends Sendable> {
    private final Message message;

    public AbstractMessageBuilder(Message message) {
        this.message = message;
    }

    /**
     * Implements, creates and returns an instance of <tt>MessageBuildSubject</tt>.
     * This is called initially when a new message is created
     *
     * @return the <tt>MessageBuildSubject</tt> with the appropriate fields
     *         and functions for the message builder
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
                try {
                    viaThis.dispatchGlobal(message);
                }
                catch (Exception e) {
                    throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
                }
            }

            public void reply() {
                RequestDispatcher dispatcher = (RequestDispatcher)
                        getIncomingMessage().getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();

                if (dispatcher == null) {
                    throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
                }

                try {
                    dispatcher.dispatch(message);
                }
                catch (Exception e) {
                    throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
                }
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
                return _sendRepeatingWith(message, viaThis, unit, interval);
            }

            public AsyncTask sendDelayedWith(final RequestDispatcher viaThis, TimeUnit unit, int interval) {
                return _sendDelayedWith(message, viaThis, unit, interval);
            }

            private AsyncTask _sendRepeatingWith(final Message message, final RequestDispatcher viaThis, TimeUnit unit, int interval) {
                final boolean isConversational = message instanceof ConversationMessageWrapper;

                final AsyncTask task = TaskManagerFactory.get().scheduleRepeating(unit, interval, new HasAsyncTaskRef() {
                    AsyncTask task;
                    AsyncDelegateErrorCallback errorCallback;

                    final Runnable sender;

                    {
                        errorCallback = new AsyncDelegateErrorCallback(this, message.getErrorCallback());

                        if (isConversational) {
                            final Message incomingMsg = ((ConversationMessageWrapper) message).getIncomingMessage();

                            if (incomingMsg.hasPart(MessageParts.ReplyTo)) {
                                sender = new Runnable() {
                                    final String replyTo = incomingMsg
                                            .get(String.class, MessageParts.ReplyTo);

                                    public void run() {
                                        try {
                                        MessageBuilder.getMessageProvider().get()
                                                .toSubject(replyTo)
                                                .copyResource("Session", incomingMsg)
                                                .addAllParts(message.getParts())
                                                .addAllProvidedParts(message.getProvidedParts())
                                                .errorsCall(errorCallback).sendNowWith(viaThis);
                                        }
                                        catch (Throwable t) {
                                            t.printStackTrace();
                                            getAsyncTask().cancel(true);
                                        }
                                    }
                                };
                            } else {
                                sender = new Runnable() {

                                    public void run() {
                                        try {
                                            MessageBuilder.getMessageProvider().get()
                                                    .copyResource("Session", incomingMsg)
                                                    .addAllParts(message.getParts())
                                                    .addAllProvidedParts(message.getProvidedParts())
                                                    .errorsCall(errorCallback).sendNowWith(viaThis);
                                        }
                                        catch (Throwable t) {
                                            t.printStackTrace();
                                            getAsyncTask().cancel(true);
                                        }

                                    }
                                };
                            }
                        } else {
                            sender = new Runnable() {
                                public void run() {
                                    try {
                                        viaThis.dispatchGlobal(MessageBuilder.getMessageProvider().get()
                                                .addAllParts(message.getParts())
                                                .addAllProvidedParts(message.getProvidedParts())
                                                .errorsCall(errorCallback));
                                    }
                                    catch (Throwable t) {
                                        t.printStackTrace();
                                        getAsyncTask().cancel(true);
                                    }
                                }
                            };
                        }
                    }

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
                        sender.run();
                    }
                });

                if (isConversational) {
                    final LaundryReclaim reclaim = LaundryListProviderFactory.get().getLaundryList(((ConversationMessageWrapper) message).getIncomingMessage().getResource(Object.class, "Session"))
                            .addToHamper(new Laundry() {
                                public void clean() {
                                    task.cancel(true);
                                }
                            });

                    task.setExitHandler(new Runnable() {
                        public void run() {
                            reclaim.reclaim();
                        }
                    });
                }

                return task;
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

        final MessageBuildCommand<R> parmBuilder = new MessageBuildCommand<R>() {
            public MessageBuildParms<R> command(Enum command) {
                message.command(command);
                return this;
            }

            public MessageBuildParms<R> command(String command) {
                message.command(command);
                return this;
            }

            public MessageBuildParms<R> signalling() {
                return this;
            }


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

            public R done() {
                return (R) sendable;
            }

            public Message getMessage() {
                return message;
            }
        };


        return new MessageBuildSubject() {
            public MessageBuildCommand<R> toSubject(String subject) {
                message.toSubject(subject);
                return parmBuilder;
            }

            public MessageBuildCommand<R> subjectProvided() {
                return parmBuilder;
            }

            public Message getMessage() {
                return message;
            }
        };
    }
}
