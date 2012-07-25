package org.jboss.errai.cdi.event.client.test;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.cdi.client.event.FunEvent;
import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.event.client.EventRoutingTestModule;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class EventRoutingIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.event.EventRoutingTestModule";
  }

  public void testEventRouting() {
    delayTestFinish(60000);


    final List<FunEvent> wireEvents = new ArrayList<FunEvent>();
    final EventRoutingTestModule module = IOC.getBeanManager().lookupBean(EventRoutingTestModule.class).getInstance();

    final Runnable verifier = new Runnable() {
      @Override
      public void run() {
        for (final FunEvent funEvent : wireEvents) {
          if (funEvent.getText().contains("A")) {
            fail("should not have received qualifier A on the wire");
          }
        }

        finishTest();
      }
    };

    ErraiBus.get().subscribe(CDI.CLIENT_DISPATCHER_SUBJECT, new MessageCallback() {
      @Override
      public void callback(Message message) {
        try {
          if (message.getCommandType() != null && CDICommands.valueOf(message.getCommandType()) == CDICommands.CDIEvent) {
            final Object beanRef = message.get(Object.class, CDIProtocol.BeanReference);
            if (beanRef instanceof FunEvent) {
              wireEvents.add((FunEvent) beanRef);
            }
          }
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        module.setResultVerifier(verifier);


        final Subscription subscribeA = CDI.subscribe(FunEvent.class.getName(), new AbstractCDIEventCallback() {
          {
            qualifierSet.add(A.class.getName());
          }

          @Override
          public void callback(Message message) {
            module.addQualifiedReceivedEvent("A", message.get(FunEvent.class, CDIProtocol.BeanReference));
          }
        });

        final Subscription subscribeB = CDI.subscribe(FunEvent.class.getName(), new AbstractCDIEventCallback() {
          {
            qualifierSet.add(B.class.getName());
          }

          @Override
          public void callback(Message message) {
            module.addQualifiedReceivedEvent("B", message.get(FunEvent.class, CDIProtocol.BeanReference));
          }
        });

        ErraiBus.get().subscribe("cdi.event:" + FunEvent.class.getName(), new MessageCallback() {
          @Override
          public void callback(Message message) {
            final Object beanRef = message.get(Object.class, CDIProtocol.BeanReference);
            if (beanRef instanceof FunEvent) {
              wireEvents.add((FunEvent) beanRef);
            }
          }
        });

        subscribeA.remove();
        module.start();
        //      subscribeA.remove();
      }
    });


  }
}
