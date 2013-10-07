package org.jboss.errai.cdi.event.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventA;

@ApplicationScoped
public class ClientLocalEventAObserver {

  MessageBus bus = ErraiBus.get();
  
  public void observer(@Observes PortableLocalEventA event) {
    MessageBuilder.createMessage(event.subject).signalling().noErrorHandling().sendNowWith(bus);
  }
  
}
