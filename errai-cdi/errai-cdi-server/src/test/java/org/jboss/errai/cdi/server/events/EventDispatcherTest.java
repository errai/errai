package org.jboss.errai.cdi.server.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jboss.errai.bus.server.api.RpcContext;
import org.junit.Before;
import org.junit.Test;

public class EventDispatcherTest {
  
  @Before
  public void setup() {
    RpcContext.remove();
  }

  @Test
  public void conversationalEventWithContext() {
    RpcContext.set(new MockMessage());
    final String convSessionId = EventDispatcher.getConversationalSessionId(MyConversationalEvent.class);
    assertEquals("bearista", convSessionId);
  }

  @Test
  public void conversationalEventWithoutContext() {
    final String convSessionId = EventDispatcher.getConversationalSessionId(MyConversationalEvent.class);
    assertNull(convSessionId);
  }
  
  @Test
  public void nonConversationalEventWithContext() {
    RpcContext.set(new MockMessage());
    final String convSessionId = EventDispatcher.getConversationalSessionId(MyNonConversationalEvent.class);
    assertNull(convSessionId);
  }
  
  @Test
  public void nonConversationalEventWithoutContext() {
    final String convSessionId = EventDispatcher.getConversationalSessionId(MyNonConversationalEvent.class);
    assertNull(convSessionId);
  }
  
}
