package org.jboss.errai.cdi.event.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventB;

@ApplicationScoped
public class ServerLocalEventBObserver {

  @Inject MessageBus bus;
  
  public void observer(@Observes PortableLocalEventB event) {
    MessageBuilder.createMessage(event.subject).signalling().noErrorHandling().sendNowWith(bus);
  }
  
}
