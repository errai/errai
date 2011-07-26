package org.jboss.errai.enterprise.client.cdi;

import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.api.Conversation;
import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

@IOCProvider
public class EventProvider implements Provider<Event<?>> {
  @Inject
  ContextualProviderContext context;

  public Event<?> get() {
    return new Event<Object>() {
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

      @Override
      public Event<Object> select(Annotation... qualifiers) {
        throw new RuntimeException("use of event selectors is unsupported");
      }

      @Override
      public <U extends Object> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        throw new RuntimeException("use of event selectors is unsupported");
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