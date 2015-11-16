package org.jboss.errai.cdi.event.server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventA;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventB;
import org.jboss.errai.cdi.event.client.test.ServerLocalEventIntegrationTest;

@ApplicationScoped
public class ServerLocalEventTestModule {

  @Inject Event<PortableLocalEventA> eventA;
  @Inject Event<PortableLocalEventB> eventB;
  
  @Service
  public void fireEventA() {
    PortableLocalEventA payload = new PortableLocalEventA();
    payload.subject = ServerLocalEventIntegrationTest.FAILURE;
    eventA.fire(payload);
  }
  
  @Service
  public void fireEventB() {
    PortableLocalEventB payload = new PortableLocalEventB();
    payload.subject = ServerLocalEventIntegrationTest.SUCCESS;
    eventB.fire(payload);
  }
}
