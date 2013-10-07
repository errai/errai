package org.jboss.errai.cdi.event.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.cdi.event.client.shared.PortableLocalEventA;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventB;
import org.jboss.errai.cdi.event.client.test.ClientLocalEventIntegrationTest;

@ApplicationScoped
public class ClientLocalEventTestModule {
  @Inject private Event<PortableLocalEventA> eventA;
  @Inject private Event<PortableLocalEventB> eventB;
  
  public void fireEventA() {
    PortableLocalEventA payload = new PortableLocalEventA();
    payload.subject = ClientLocalEventIntegrationTest.SUCCESS;
    eventA.fire(payload);
  }
  
  public void fireEventB() {
    PortableLocalEventB payload = new PortableLocalEventB();
    payload.subject = ClientLocalEventIntegrationTest.FAILURE;
    eventB.fire(payload);
  }
}
