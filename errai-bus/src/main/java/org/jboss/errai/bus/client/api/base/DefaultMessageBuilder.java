/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api.base;

import static org.jboss.errai.bus.client.api.base.ConversationHelper.createConversationService;
import static org.jboss.errai.bus.client.api.base.ConversationHelper.makeConversational;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.builder.MessageBuildCommand;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendable;
import org.jboss.errai.bus.client.api.builder.MessageBuildSubject;
import org.jboss.errai.bus.client.api.builder.MessageReplySendable;
import org.jboss.errai.bus.client.api.builder.Sendable;
import org.jboss.errai.bus.client.api.laundry.Laundry;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.jboss.errai.bus.client.api.laundry.LaundryReclaim;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.HasAsyncTaskRef;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.TimeUnit;

/**
 * Part of the implementation of the fluent API whose entry point is {@link MessageBuilder}.
 *
 * @author Mike Brock
 * @author Max Barkley <mbarkley@redhat.com>
 */
@SuppressWarnings({"unchecked"})
class DefaultMessageBuilder<R extends Sendable> {
  private final Message message;

  public DefaultMessageBuilder(final Message message) {
    this.message = message;
  }

  /**
   * Implements, creates and returns an instance of <tt>MessageBuildSubject</tt>.
   * This is called initially when a new message is created
   *
   * @return the <tt>MessageBuildSubject</tt> with the appropriate fields
   *         and functions for the message builder
   */
  public MessageBuildSubject<R> start() {
    final Sendable sendable = new MessageReplySendable() {
      boolean reply = false;

      @Override
      public MessageBuildSendable repliesToSubject(String subjectName) {
        message.set(MessageParts.ReplyTo, subjectName);
        return this;
      }

      @Override
      public MessageBuildSendable repliesTo(final MessageCallback callback) {
        reply = true;
        makeConversational(message, callback);
        return this;
      }

      @Override
      public void sendNowWith(final MessageBus viaThis) {
        message.sendNowWith(viaThis);
      }

      @Override
      public void sendNowWith(final MessageBus viaThis, final boolean fireMessageListener) {
        if (reply) createConversationService(viaThis, message);
        viaThis.send(message, false);
      }

      @Override
      public void sendNowWith(final RequestDispatcher viaThis) {
        message.sendNowWith(viaThis);
      }

      @Override
      public void sendGlobalWith(final MessageBus viaThis) {
        if (reply) createConversationService(viaThis, message);

        viaThis.sendGlobal(message);
      }

      @Override
      public void sendGlobalWith(final RequestDispatcher viaThis) {
        try {
          viaThis.dispatchGlobal(message);
        }
        catch (Exception e) {
          throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
        }
      }

      @Override
      public void reply() {
        final Message incomingMessage = getIncomingMessage();

        if (incomingMessage == null) {
          throw new IllegalStateException("Cannot reply.  Cannot find incoming message.");
        }

        if (!incomingMessage.hasResource(RequestDispatcher.class.getName())) {
          throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
        }

        final RequestDispatcher dispatcher = (RequestDispatcher)
                incomingMessage.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();

        if (dispatcher == null) {
          throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
        }

        final Message msg = getIncomingMessage();

        message.copyResource("Session", msg);
        message.copyResource(RequestDispatcher.class.getName(), msg);

        try {
          dispatcher.dispatch(message);
        }
        catch (Exception e) {
          throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
        }
      }

      @Override
      public AsyncTask replyRepeating(final TimeUnit unit, final int interval) {
        final Message msg = getIncomingMessage();
        message.copyResource("Session", msg);
        final RequestDispatcher dispatcher = (RequestDispatcher) msg.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();
        return _sendRepeatingWith(message, dispatcher, unit, interval);
      }

      @Override
      public AsyncTask replyDelayed(final TimeUnit unit, final int interval) {
        final Message msg = getIncomingMessage();
        message.copyResource("Session", msg);
        final RequestDispatcher dispatcher = (RequestDispatcher) msg.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();
        return _sendDelayedWith(message, dispatcher, unit, interval);
      }

      private Message getIncomingMessage() {
        return ((ConversationMessageWrapper) message).getIncomingMessage();
      }

      @Override
      public AsyncTask sendRepeatingWith(final RequestDispatcher viaThis, final TimeUnit unit, final int interval) {
        return _sendRepeatingWith(message, viaThis, unit, interval);
      }

      @Override
      public AsyncTask sendDelayedWith(final RequestDispatcher viaThis, final TimeUnit unit, final int interval) {
        return _sendDelayedWith(message, viaThis, unit, interval);
      }

      private AsyncTask _sendRepeatingWith(final Message message, final RequestDispatcher viaThis, final TimeUnit unit, final int interval) {
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

                  @Override
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
              }
              else {
                sender = new Runnable() {

                  @Override
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
            }
            else {
              sender = new Runnable() {
                @Override
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

          @Override
          public void setAsyncTask(final AsyncTask task) {
            synchronized (this) {
              this.task = task;
            }
          }


          @Override
          public AsyncTask getAsyncTask() {
            synchronized (this) {
              return task;
            }
          }

          @Override
          public void run() {
            sender.run();
          }
        });

        if (isConversational) {
          final Object sessionResource = ((ConversationMessageWrapper) message).getIncomingMessage().getResource(Object.class, "Session");
          final LaundryReclaim reclaim =
                  LaundryListProviderFactory.get().getLaundryList(sessionResource).add(new Laundry() {
                            @Override
                            public void clean() {
                              task.cancel(true);
                            }
                          });

          task.setExitHandler(new Runnable() {
            @Override
            public void run() {
              reclaim.reclaim();
            }
          });
        }

        return task;
      }


      public AsyncTask _sendDelayedWith(final Message message, final RequestDispatcher viaThis, final TimeUnit unit, final int interval) {
        return TaskManagerFactory.get().schedule(unit, interval, new HasAsyncTaskRef() {
          AsyncTask task;
          AsyncDelegateErrorCallback errorCallback
                  = new AsyncDelegateErrorCallback(this, message.getErrorCallback());

          @Override
          public void setAsyncTask(final AsyncTask task) {
            synchronized (this) {
              this.task = task;
            }
          }


          @Override
          public AsyncTask getAsyncTask() {
            synchronized (this) {
              return task;
            }
          }

          @Override
          public void run() {
            MessageBuilder.getMessageProvider().get()
                    .copyResource("Session", message)
                    .addAllParts(message.getParts())
                    .addAllProvidedParts(message.getProvidedParts())
                    .errorsCall(errorCallback).sendNowWith(viaThis);
          }
        });
      }


      @Override
      public Message getMessage() {
        return message;
      }
    };

    final MessageBuildCommand<R> parmBuilder = new MessageBuildCommand<R>() {
      @Override
      public MessageBuildParms<R> command(final Enum<?> command) {
        message.command(command);
        return this;
      }

      @Override
      public MessageBuildParms<R> command(final String command) {
        message.command(command);
        return this;
      }

      @Override
      public MessageBuildParms<R> signalling() {
        return this;
      }

      @Override
      public MessageBuildParms<R> withValue(final Object value) {
        message.set(MessageParts.Value, value);
        return this;
      }

      @Override
      public MessageBuildParms<R> with(final String part, final Object value) {
        message.set(part, value);
        return this;
      }

      @Override
      public MessageBuildParms<R> flag(final RoutingFlag flag) {
        message.setFlag(flag);
        return this;
      }

      @Override
      public MessageBuildParms<R> with(final Enum<?> part, final Object value) {
        message.set(part, value);
        return this;
      }

      @Override
      public MessageBuildParms<R> withProvided(final String part, final ResourceProvider<?> provider) {
        message.setProvidedPart(part, provider);
        return this;
      }

      @Override
      public MessageBuildParms<R> withProvided(final Enum<?> part, final ResourceProvider<?> provider) {
        message.setProvidedPart(part, provider);
        return this;
      }

      @Override
      public MessageBuildParms<R> copy(final String part, final Message m) {
        message.copy(part, m);
        return this;
      }

      @Override
      public MessageBuildParms<R> copy(final Enum<?> part, final Message m) {
        message.copy(part, m);
        return this;
      }

      @Override
      public MessageBuildParms<R> copyResource(final String part, final Message m) {
        message.copyResource(part, m);
        return this;
      }
      // XXX why does this return R?
      @Override
      public R errorsHandledBy(@SuppressWarnings("rawtypes") final ErrorCallback callback) {
        message.errorsCall(callback);
        message.set(MessageParts.ErrorTo, DefaultErrorCallback.CLIENT_ERROR_SUBJECT);
        return (R) sendable;

      }

      @Override
      public R noErrorHandling() {
        message.errorsCall(NoHandlingErrorCallback.INSTANCE);
        return (R) sendable;
      }

      @Override
      public R defaultErrorHandling() {
        message.errorsCall(DefaultErrorCallback.INSTANCE);
        message.set(MessageParts.ErrorTo, DefaultErrorCallback.CLIENT_ERROR_SUBJECT);
        return (R) sendable;
      }

      @Override
      public R done() {
        if (message.getErrorCallback() == null) {
          defaultErrorHandling();
        }
        return (R) sendable;
      }

      @Override
      public Message getMessage() {
        return message;
      }
    };


    return new MessageBuildSubject<R>() {
      @Override
      public MessageBuildCommand<R> toSubject(final String subject) {
        message.toSubject(subject);
        return parmBuilder;
      }

      @Override
      public MessageBuildCommand<R> subjectProvided() {
        return parmBuilder;
      }

      @Override
      public Message getMessage() {
        return message;
      }
    };
  }
}
