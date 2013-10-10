package org.jboss.errai.cdi.event.client.test;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.cdi.client.event.LocalEventA;
import org.jboss.errai.cdi.client.event.MyEventImpl;
import org.jboss.errai.cdi.event.client.shared.PortableLocalEventA;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.junit.Test;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class EventAdvertisingIntegrationTest extends AbstractErraiCDITest {

  private final List<String> messageBeanTypeLog = new ArrayList<String>();
  private ClientMessageBusImpl backupBus;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventObserverTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    ClientMessageBusImpl fakeBus = new ClientMessageBusImpl() {
      @Override
      public void send(Message message) {
        if (message.hasPart(CDIProtocol.BeanType) && message.getSubject().equals(CDI.SERVER_DISPATCHER_SUBJECT)) {
          messageBeanTypeLog.add(message.get(String.class, CDIProtocol.BeanType));
        }
        super.send(message);
      }
    };

    backupBus = UntestableFrameworkUtil.installAlternativeBusImpl(fakeBus);

    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    UntestableFrameworkUtil.installAlternativeBusImpl(backupBus);
  }

  @Test
  public void testLocalEventNotInitiallyAdvertisedToServer() {

    // this is the actual point of the test
    assertFalse("Local event should not have been advertised to the server", messageBeanTypeLog.contains(LocalEventA.class.getName()));

    // this is an important safety check, because it would be too easy for the test to fake-pass if the implementation details change.
    assertTrue("Portable event should have been advertised to the server", messageBeanTypeLog.contains(MyEventImpl.class.getName()));
  }

  @Test
  public void testLocalEventNotReadvertisedToServer() {
    CDI.resendSubscriptionRequestForAllEventTypes();

    // this is the actual point of the test
    assertFalse("Local event should not have been advertised to the server", messageBeanTypeLog.contains(LocalEventA.class.getName()));

    // this is an important safety check, because it would be too easy for the test to fake-pass if the implementation details change.
    assertTrue("Portable event should have been advertised to the server", messageBeanTypeLog.contains(MyEventImpl.class.getName()));
  }
  
  @Test
  public void testPortableLocalEventNotInitiallyAdvertisedToServer() {
    
    // this is the actual point of the test
    assertFalse("Local event should not have been advertised to the server", messageBeanTypeLog.contains(PortableLocalEventA.class.getName()));

    // this is an important safety check, because it would be too easy for the test to fake-pass if the implementation details change.
    assertTrue("Portable event should have been advertised to the server", messageBeanTypeLog.contains(MyEventImpl.class.getName()));
  }
  
  @Test
  public void testPortableLocalEventNotReadvertisedToServer() {
    CDI.resendSubscriptionRequestForAllEventTypes();

    // this is the actual point of the test
    assertFalse("Local event should not have been advertised to the server", messageBeanTypeLog.contains(PortableLocalEventA.class.getName()));

    // this is an important safety check, because it would be too easy for the test to fake-pass if the implementation details change.
    assertTrue("Portable event should have been advertised to the server", messageBeanTypeLog.contains(MyEventImpl.class.getName()));
  }

}
