package org.jboss.errai.cdi.client;

import org.jboss.errai.cdi.client.api.Event;
import org.jboss.errai.ioc.client.api.Provider;
import org.jboss.errai.ioc.client.api.TypeProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.lang.annotation.Annotation;

/**
 * User: christopherbrock
 * Date: 27-Jul-2010
 * Time: 4:24:02 PM
 */
@Provider
@ApplicationScoped
public class EventProvider implements TypeProvider<Event>
{
  //@Produces
  //@Dependent
  public Event provide() {
    
    return new Event() {
      public void fire(Object event) {
        CDI.fireEvent(event);
      }

      public Event select(Annotation... qualifiers) {
        throw new RuntimeException("Not implemented");
      }

      public Event select(Class subtype, Annotation... qualifiers) {
        throw new RuntimeException("Not implemented");
      }

    };
  }
}