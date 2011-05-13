package org.jboss.errai.cdi.client;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.api.Conversation;
import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.Window;

@IOCProvider
public class EventProvider implements ContextualTypeProvider<Event<?>> {

    public Event<?> provide(final Class[] typeargs, final Annotation[] qualifiers) {
    	return new Event() {
            private Class eventType = (typeargs.length == 1 ? typeargs[0] : Object.class);
            private Conversation conversation;
            private Annotation[] _qualifiers = qualifiers;
            
            public void fire(Object event) {
                if (event == null) return;
                if (conversation != null && !conversation.isActive()) {
                    conversation.begin();
                }

                CDI.fireEvent(event, qualifiers);
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
            	return qualifiers;
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