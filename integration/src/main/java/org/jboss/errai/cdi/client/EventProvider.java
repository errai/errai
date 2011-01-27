package org.jboss.errai.cdi.client;

import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.Provider;
import org.jboss.errai.ioc.client.api.TypeProvider;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;

/**
 * User: christopherbrock
 * Date: 27-Jul-2010
 * Time: 4:24:02 PM
 */
@Provider
@ApplicationScoped
public class EventProvider implements ContextualTypeProvider<Event<?>> {

    public Event<?> provide(final Class[] typeargs) {
        return new Event() {
            private Class eventType = (typeargs.length == 1 ? typeargs[0] : Object.class);

            public void fire(Object event) {
                if (event == null) return;

                CDI.fireEvent(event);
            }

            public Event select(Annotation... qualifiers) {
                throw new RuntimeException("Not implemented");
            }

            public Event select(Class subtype, Annotation... qualifiers) {
                throw new RuntimeException("Not implemented");
            }

            public Class getEventType() {
                return eventType;
            }
        };
    }
}