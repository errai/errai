package org.jboss.errai.cdi.server;

/**
 * @author Mike Brock .
 */
public interface ConversationalEvent<R, S> {
  public R getEvent();

  public void fire(S s);
}
