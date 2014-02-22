package org.jboss.errai.ioc.tests.lifecycle.client.local;

import static org.junit.Assert.*;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleCallback;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerGenerator;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerRegistrar;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.junit.Test;

public class IOCLifecycleTest extends AbstractErraiIOCTest {
  
  public static class Counter {
    private int counter = 0;
    public void add(final int number) {
      counter += number;
    }
    public int getValue() {
      return counter;
    }
  }

  @Test
  public void testSingleLifecycleListenerIsCalled() {
    // Build listener and generator
    final Counter listenerCounter = new Counter();
    final LifecycleListener<Integer> listener = new LifecycleListener<Integer>() {
      @Override
      public void observeEvent(LifecycleEvent<Integer> event) {
        listenerCounter.add(1);
      }
      @Override
      public boolean isObserveableEventType(Class<? extends LifecycleEvent<Integer>> eventType) {
        return eventType.equals(Access.class);
      }
    };
    final LifecycleListenerGenerator<Integer> generator = new LifecycleListenerGenerator<Integer>() {
      @Override
      public LifecycleListener<Integer> newInstance() {
        return listener;
      }
    };

    // Build event
    final Integer instance = 1337;
    final Access<Integer> event = IOC.getBeanManager().lookupBean(Access.class).getInstance();
    event.setInstance(instance);

    // Register listener
    final LifecycleListenerRegistrar registrar = IOC.getBeanManager()
            .lookupBean(LifecycleListenerRegistrar.class).getInstance();
    registrar.registerListener(Integer.class, generator);

    // Precondition
    assertEquals(0, listenerCounter.getValue());
    
    final Counter callbackCounter = new Counter();

    event.fireAsync(new LifecycleCallback() {
      @Override
      public void callback(boolean success) {
        assertTrue(success);
        callbackCounter.add(1);
      }
    });

    assertEquals(1, listenerCounter.getValue());
    assertEquals(1, callbackCounter.getValue());
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.lifecycle.LifecycleTests";
  }

}
