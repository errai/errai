/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.api.base;

import static org.jboss.errai.bus.client.api.base.ConversationHelper.createConversationService;
import static org.jboss.errai.bus.client.api.base.ConversationHelper.makeConversational;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.HasAsyncTaskRef;
import org.jboss.errai.bus.client.api.Laundry;
import org.jboss.errai.bus.client.api.LaundryReclaim;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.builder.MessageBuildCommand;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendable;
import org.jboss.errai.bus.client.api.builder.MessageBuildSubject;
import org.jboss.errai.bus.client.api.builder.MessageReplySendable;
import org.jboss.errai.bus.client.api.builder.Sendable;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * The <tt>AbstractMessageBuilder</tt> facilitates the building of a message,
 * and ensures that it is created and used properly.
 *
 * @author Mike Brock
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

      @Override
      public MessageBuildSendable repliesTo(MessageCallback callback) {
        reply = true;
        makeConversational(message, callback);
        return this;
      }

      @Override
      public void sendNowWith(MessageBus viaThis) {
        if (reply) createConversationService(viaThis, message);
        message.sendNowWith(viaThis);
      }

      @Override
      public void sendNowWith(MessageBus viaThis, boolean fireMessageListener) {
        if (reply) createConversationService(viaThis, message);
        viaThis.send(message, false);
      }

      @Override
      public void sendNowWith(RequestDispatcher viaThis) {
        message.sendNowWith(viaThis);
      }

      @Override
      public void sendGlobalWith(MessageBus viaThis) {
        viaThis.sendGlobal(message);
      }

      @Override
      public void sendGlobalWith(RequestDispatcher viaThis) {
        try {
          viaThis.dispatchGlobal(message);
        }
        catch (Exception e) {
          throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
        }
      }

      @Override
      public void reply() {
        Message incomingMessage = getIncomingMessage();

        if (incomingMessage == null) {
          throw new IllegalStateException("Cannot reply.  Cannot find incoming message.");
        }

        if (!incomingMessage.hasResource(RequestDispatcher.class.getName())) {
          throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
        }

        RequestDispatcher dispatcher = (RequestDispatcher)
                incomingMessage.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();

        if (dispatcher == null) {
          throw new IllegalStateException("Cannot reply.  Cannot find RequestDispatcher resource.");
        }

        Message msg = getIncomingMessage();

        message.copyResource("Session", msg);

        try {
          dispatcher.dispatch(message);
        }
        catch (Exception e) {
          throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
        }
      }

      @Override
      public AsyncTask replyRepeating(TimeUnit unit, int interval) {
        Message msg = getIncomingMessage();
        message.copyResource("Session", msg);
        RequestDispatcher dispatcher = (RequestDispatcher) msg.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();
        return _sendRepeatingWith(message, dispatcher, unit, interval);
      }

      @Override
      public AsyncTask replyDelayed(TimeUnit unit, int interval) {
        Message msg = getIncomingMessage();
        message.copyResource("Session", msg);
        RequestDispatcher dispatcher = (RequestDispatcher) msg.getResource(ResourceProvider.class, RequestDispatcher.class.getName()).get();
        return _sendDelayedWith(message, dispatcher, unit, interval);
      }

      private Message getIncomingMessage() {
        return ((ConversationMessageWrapper) message).getIncomingMessage();
      }

      @Override
      public AsyncTask sendRepeatingWith(final RequestDispatcher viaThis, TimeUnit unit, int interval) {
        return _sendRepeatingWith(message, viaThis, unit, interval);
      }

      @Override
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
          public void setAsyncTask(AsyncTask task) {
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
          final LaundryReclaim reclaim =
                  LaundryListProviderFactory.get().getLaundryList(((ConversationMessageWrapper) message).getIncomingMessage().getResource(Object.class, "Session"))
                          .addToHamper(new Laundry() {
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


      public AsyncTask _sendDelayedWith(final Message message, final RequestDispatcher viaThis, TimeUnit unit, int interval) {
        return TaskManagerFactory.get().schedule(unit, interval, new HasAsyncTaskRef() {
          AsyncTask task;
          AsyncDelegateErrorCallback errorCallback
                  = new AsyncDelegateErrorCallback(this, message.getErrorCallback());

          @Override
          public void setAsyncTask(AsyncTask task) {
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
      public MessageBuildParms<R> command(Enum command) {
        message.command(command);
        return this;
      }

      @Override
      public MessageBuildParms<R> command(String command) {
        message.command(command);
        return this;
      }

      @Override
      public MessageBuildParms<R> signalling() {
        return this;
      }

      @Override
      public MessageBuildParms<R> withValue(Object value) {
        message.set(MessageParts.Value, value);
        return this;
      }

      @Override
      public MessageBuildParms<R> with(String part, Object value) {
        message.set(part, value);
        return this;
      }

      @Override
      public MessageBuildParms<R> flag(RoutingFlags flag) {
        message.setFlag(flag);
        return this;
      }

      @Override
      public MessageBuildParms<R> with(Enum part, Object value) {
        message.set(part, value);
        return this;
      }

      @Override
      public MessageBuildParms<R> withProvided(String part, ResourceProvider provider) {
        message.setProvidedPart(part, provider);
        return this;
      }

      @Override
      public MessageBuildParms<R> withProvided(Enum part, ResourceProvider provider) {
        message.setProvidedPart(part, provider);
        return this;
      }

      @Override
      public MessageBuildParms<R> copy(String part, Message m) {
        message.copy(part, m);
        return this;
      }

      @Override
      public MessageBuildParms<R> copy(Enum part, Message m) {
        message.copy(part, m);
        return this;
      }

      @Override
      public MessageBuildParms<R> copyResource(String part, Message m) {
        message.copyResource(part, m);
        return this;
      }

      @Override
      public R errorsHandledBy(ErrorCallback callback) {
        message.errorsCall(callback);
        return (R) sendable;

      }

      @Override
      public R noErrorHandling() {
        return (R) sendable;
      }

      @Override
      public R defaultErrorHandling() {
        message.errorsCall(DefaultErrorCallback.INSTANCE);
        return (R) sendable;
      }

      @Override
      public R done() {
        return (R) sendable;
      }

      @Override
      public Message getMessage() {
        return message;
      }
    };


    return new MessageBuildSubject() {
      @Override
      public MessageBuildCommand<R> toSubject(String subject) {
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
