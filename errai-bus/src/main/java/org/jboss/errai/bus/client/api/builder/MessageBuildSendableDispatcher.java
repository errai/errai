/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;

/**
 * @author Mike Brock .
 */
public interface MessageBuildSendableDispatcher extends MessageBuildSendable {


  /**
   * Sends the message with the specified <tt>RequestDispatcher</tt>.
   *
   * @param viaThis
   *          the dispatcher to send the message with, usually obtained in
   *          client code via Errai IOC dependency injection.
   */
  public void sendNowWith(RequestDispatcher viaThis);

  /**
   * Sends the message globally with the specified <tt>RequestDispatcher</tt>.
   *
   * @param viaThis
   *          the dispatcher to send the message with, usually obtained in
   *          client code via Errai IOC dependency injection.
   */
  public void sendGlobalWith(RequestDispatcher viaThis);

  /**
   * Sends the message periodically with the specified
   * <tt>RequestDispatcher</tt>.
   *
   * @param viaThis
   *          the dispatcher to send the message with, usually obtained in
   *          client code via Errai IOC dependency injection.
   * @param unit
   *          The time unit that {@code interval} should be interpreted as.
   * @param interval
   *          The amount of time to wait between message retransmissions (units
   *          specified by the {@code unit} parameter).
   * @return A handle on the repeating task which can be used to cancel it.
   */
  public AsyncTask sendRepeatingWith(RequestDispatcher viaThis, TimeUnit unit, int interval);

  /**
   * Sends the message after a specified delay with the specified
   * <tt>RequestDispatcher</tt>.
   *
   * @param viaThis
   *          the dispatcher to send the message with, usually obtained in
   *          client code via Errai IOC dependency injection.
   * @param unit
   *          The time unit that {@code interval} should be interpreted as.
   * @param interval
   *          The amount of time to wait before sending the message (units
   *          specified by the {@code unit} parameter).
   * @return A handle on the repeating task which can be used to cancel it.
   */
  public AsyncTask sendDelayedWith(RequestDispatcher viaThis, TimeUnit unit, int interval);

}
