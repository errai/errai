package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.MessageParts;


public class AbstractRemoteCallBuilder {
    private volatile static int callCounter = 0;

    private final Message message;
    private RemoteCallback remoteCallback;
    private Class<?> responseType = Object.class;

    public AbstractRemoteCallBuilder(Message message) {
        this.message = message;
    }

    public RemoteCallEndpointDef call(final String serviceName) {
        message.toSubject(serviceName);

        final RemoteCallSendable sendable = new RemoteCallSendable() {
            public void sendNowWith(final MessageBus bus) {
                if (remoteCallback != null) {
                    final String replyTo = message.getSubject() + "." + message.getCommandType() + ":RespondTo:" + uniqueNumber();

                    if (remoteCallback != null) {
                        bus.subscribe(replyTo,
                                new MessageCallback() {
                                    public void callback(Message message) {
                                        bus.unsubscribeAll(replyTo);
                                        remoteCallback.callback(message.get(responseType, "MethodReply"));
                                    }
                                });
                        message.set(MessageParts.ReplyTo, replyTo);
                    }
                }
                message.sendNowWith(bus);
            }
        };

        final RemoteCallErrorDef errorDef = new RemoteCallErrorDef() {
            public RemoteCallSendable errorsHandledBy(ErrorCallback errorCallback) {
                message.errorsCall(errorCallback);
                return sendable;
            }

            public RemoteCallSendable noErrorHandling() {
                return sendable;
            }
        };

        final RemoteCallResponseDef respondDef = new RemoteCallResponseDef() {
            public RemoteCallErrorDef respondTo(RemoteCallback callback) {
                remoteCallback = callback;
                return errorDef;
            }

            public <T> RemoteCallErrorDef respondTo(Class<T> returnType, RemoteCallback<T> callback) {
                responseType = returnType;
                remoteCallback = callback;
                return errorDef;
            }
        };

        return new RemoteCallEndpointDef() {
            public RemoteCallResponseDef endpoint(String endPointName) {
                message.command(endPointName);
                return respondDef;
            }

            public RemoteCallResponseDef endpoint(String endPointName, Object... args) {
                message.command(endPointName);
                if (args != null) message.set("MethodParms", args);
                return respondDef;
            }
        };

    }

    private static int uniqueNumber() {
        return ++callCounter > 10000 ? callCounter = 0 : callCounter;
    }
}