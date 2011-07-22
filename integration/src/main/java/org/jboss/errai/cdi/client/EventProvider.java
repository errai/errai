package org.jboss.errai.cdi.client;

import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.api.Conversation;
import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

@IOCProvider
public class EventProvider implements Provider<Event<?>> {
  @Inject
  ContextualProviderContext context;

  public Event<?> get() {
    return new Event() {
      private Class eventType = (context.getTypeArguments().length == 1 ? context.getTypeArguments()[0] : Object.class);
      private Conversation conversation;
      private Annotation[] _qualifiers = context.getQualifiers();

      public void fire(Object event) {
        if (event == null)
          return;
        if (conversation != null && !conversation.isActive()) {
          conversation.begin();
        }

        CDI.fireEvent(event, _qualifiers);
      }

      public Event select(Annotation... qualifiers) {
        _qualifiers = qualifiers;
        return this;
      }

      public Event select(Class subtype, Annotation... qualifiers) {
        throw new RuntimeException("Not implemented");
      }

      public Class getEventType() {
        return eventType;
      }

      public Annotation[] getQualifiers() {
        return _qualifiers;
      }

      public void registerConversation(Conversation conversation) {
        endActiveConversation();
        this.conversation = conversation;
      }

      public void endActiveConversation() {
        if (conversation != null && conversation.isActive()) {
          conversation.end();
        }
      }
    };
  }
}