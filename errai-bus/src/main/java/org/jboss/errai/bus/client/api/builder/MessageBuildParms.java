package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.ErrorCallback;
import org.jboss.errai.bus.client.Message;

public interface MessageBuildParms {
         public MessageBuildParms with(String part, Object value);

        public MessageBuildParms with(Enum part, Object value);

        public MessageBuildParms copy(String part, Message m);

        public MessageBuildParms copy(Enum part, Message m);

        public MessageBuildSendable errorsHandledBy(ErrorCallback callback);

        public MessageBuildSendable noErrorHandling();
}
