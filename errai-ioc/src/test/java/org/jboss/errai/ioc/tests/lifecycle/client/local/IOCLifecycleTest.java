package org.jboss.errai.ioc.tests.lifecycle.client.local;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleCallback;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerGenerator;
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

  private static class CountingListener implements LifecycleListener<Integer> {
    private final Counter listenerCounter;

    private CountingListener(final Counter counter) {
      listenerCounter = counter;
    }

    @Override
    public void observeEvent(LifecycleEvent<Integer> event) {
      listenerCounter.add(1);
    }

    @Override
    public boolean isObserveableEventType(Class<? extends LifecycleEvent<Integer>> eventType) {
      return eventType.equals(Access.class);
    }
  }

  @Test
  public void testSingleLifecycleListenerIsCalled() {
    // Build listener and generator
    final Counter listenerCounter = new Counter();
    final LifecycleListener<Integer> listener = new CountingListener(listenerCounter);
    final LifecycleListenerGenerator<Integer> generator = new LifecycleListenerGenerator<Integer>() {
      @Override
      public LifecycleListener<Integer> newInstance() {
        return listener;
      }
    };

    // Build event
    final Integer instance = 1337;
    final Access<Integer> event = IOC.getBeanManager().lookupBean(Access.class).getInstance();

    // Register listener
    IOC.registerIOCLifecycleListener(Integer.class, generator);

    // Precondition
    assertEquals(0, listenerCounter.getValue());

    final Counter callbackCounter = new Counter();

    event.fireAsync(instance, new LifecycleCallback() {
      @Override
      public void callback(boolean success) {
        assertTrue(success);
        callbackCounter.add(1);
      }
    });

    assertEquals(1, listenerCounter.getValue());
    assertEquals(1, callbackCounter.getValue());
  }

  @Test
  public void testLifecycleListenerIsUnsubscribedSameInstance() throws Exception {
    // Build listener and generator
    final Counter listenerCounter = new Counter();
    final LifecycleListener<Integer> listener = new CountingListener(listenerCounter);
    final LifecycleListenerGenerator<Integer> generator = new LifecycleListenerGenerator<Integer>() {
      @Override
      public LifecycleListener<Integer> newInstance() {
        return listener;
      }
    };

    // Build event
    final Integer instance = 1337;
    final Access<Integer> event = IOC.getBeanManager().lookupBean(Access.class).getInstance();

    // Register listener
    IOC.registerIOCLifecycleListener(Integer.class, generator);

    // Precondition
    assertEquals(0, listenerCounter.getValue());

    final Counter callbackCounter = new Counter();
    final LifecycleCallback callback = new LifecycleCallback() {
      @Override
      public void callback(boolean success) {
        assertTrue(success);
        callbackCounter.add(1);
      }
    };
    event.fireAsync(instance, callback);

    // Still precondition
    assertEquals(1, listenerCounter.getValue());
    assertEquals(1, callbackCounter.getValue());

    // Unregister
    IOC.unregisterIOCLifecycleListener(Integer.class, generator);

    event.fireAsync(instance, callback);

    assertEquals(1, listenerCounter.getValue());
  }

  @Test
  public void testLifecycleListenerIsUnsubscribedNewInstance() throws Exception {
    // Build listener and generator
    final Counter listenerCounter = new Counter();
    final LifecycleListener<Integer> listener = new CountingListener(listenerCounter);
    final LifecycleListenerGenerator<Integer> generator = new LifecycleListenerGenerator<Integer>() {
      @Override
      public LifecycleListener<Integer> newInstance() {
        return listener;
      }
    };

    // Build event
    final Integer instance = 1337;
    final Access<Integer> event = IOC.getBeanManager().lookupBean(Access.class).getInstance();

    // Register listener
    IOC.registerIOCLifecycleListener(Integer.class, generator);

    // Precondition
    assertEquals(0, listenerCounter.getValue());

    final Counter callbackCounter = new Counter();
    final LifecycleCallback callback = new LifecycleCallback() {
      @Override
      public void callback(boolean success) {
        assertTrue(success);
        callbackCounter.add(1);
      }
    };
    event.fireAsync(instance, callback);

    // Still precondition
    assertEquals(1, listenerCounter.getValue());
    assertEquals(1, callbackCounter.getValue());

    // Unregister
    IOC.unregisterIOCLifecycleListener(Integer.class, generator);

    final Integer newInstance = 649;
    event.fireAsync(newInstance, callback);

    assertEquals(1, listenerCounter.getValue());
  }

  @Test
  public void testListenersNotSharedAcrossInstances() throws Exception {
    // Build listeners and generator
    final Counter firstCounter = new Counter();
    final LifecycleListener<Integer> firstListener = new CountingListener(firstCounter);
    final Counter secondCounter = new Counter();
    final LifecycleListener<Integer> secondListener = new CountingListener(secondCounter);
    final LifecycleListenerGenerator<Integer> generator = new LifecycleListenerGenerator<Integer>() {
      private int generated = 0;

      @Override
      public LifecycleListener<Integer> newInstance() {
        generated += 1;
        if (generated == 1)
          return firstListener;
        else if (generated == 2)
          return secondListener;
        else
          throw new IllegalStateException("Test should only call this generator twice.");
      }
    };

    // Build event and instances
    final Integer firstInstance = 1337;
    final Integer secondInstance = 649;
    final Access<Integer> event = IOC.getBeanManager().lookupBean(Access.class).getInstance();

    final Counter callbackCounter = new Counter();
    final LifecycleCallback callback = new LifecycleCallback() {
      @Override
      public void callback(boolean success) {
        assertTrue(success);
        callbackCounter.add(1);
      }
    };
    
    // Register generator
    IOC.registerIOCLifecycleListener(Integer.class, generator);
    
    // Precondition
    assertEquals(0, firstCounter.getValue());
    assertEquals(0, secondCounter.getValue());
    assertEquals(0, callbackCounter.getValue());

    // Fire event on first instance.
    event.fireAsync(firstInstance, callback);
    
    assertEquals(1, callbackCounter.getValue());
    assertEquals(1, firstCounter.getValue());
    assertEquals(0, secondCounter.getValue());
    
    // Fire event on second instance.
    event.fireAsync(secondInstance, callback);

    assertEquals(2, callbackCounter.getValue());
    assertEquals(1, firstCounter.getValue());
    assertEquals(1, secondCounter.getValue());
  }
  
  @Test
  public void testRegisterSingleInstanceListener() throws Exception {
    final Counter listenerCounter = new Counter();
    final Counter callbackCounter = new Counter();
    final LifecycleListener<Integer> listener = new CountingListener(listenerCounter);
    
    final Integer instance = 1337;
    
    final Access<Integer> event = IOC.getBeanManager().lookupBean(Access.class).getInstance();
    final LifecycleCallback callback = new LifecycleCallback() {
      @Override
      public void callback(boolean success) {
        assertTrue(success);
        callbackCounter.add(1);
      }
    };
    
    IOC.registerInstanceListener(instance, listener);
    
    // Precondition
    assertEquals(0, callbackCounter.getValue());
    assertEquals(0, listenerCounter.getValue());
    
    event.fireAsync(instance, callback);
    
    assertEquals(1, callbackCounter.getValue());
    assertEquals(1, listenerCounter.getValue());
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.lifecycle.LifecycleTests";
  }

}
