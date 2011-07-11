package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

/**
 * @author Mike Brock .
 */
public interface MessageBuildSendableDispatcher extends MessageBuildSendable {


  /**
   * Sends the message with the specified <tt>RequestDispatcher</tt>
   *
   * @param viaThis - the dispatcher to send the message with
   */
  public void sendNowWith(RequestDispatcher viaThis);

  /**
   * Sends the message globally with the specified <tt>RequestDispatcher</tt>
   *
   * @param viaThis - the dispatcher to send the message with
   */
  public void sendGlobalWith(RequestDispatcher viaThis);


  public AsyncTask sendRepeatingWith(RequestDispatcher viaThis, TimeUnit unit, int interval);

  public AsyncTask sendDelayedWith(RequestDispatcher viaThis, TimeUnit unit, int interval);

}
