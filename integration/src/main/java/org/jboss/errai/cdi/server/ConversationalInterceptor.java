package org.jboss.errai.cdi.server;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.cdi.client.api.Conversational;

/**
 * @author Mike Brock .
 */

@Interceptor @Conversational public class ConversationalInterceptor {

  @Inject private ContextManager ctxManager;

  @AroundInvoke public Object invokeConversationalObserver(InvocationContext context) {
    try {
      return context.proceed();
    } catch (Exception e) {
      throw new RuntimeException("failure in interceptor", e);
    } finally {
      ctxManager.getRequestContextStore().remove(MessageParts.SessionID.name());
    }
  }
}
