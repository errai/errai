/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.PreInitializationListener;
import org.jboss.errai.bus.client.api.SessionExpirationListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extended client-specific/in-browser interface of {@link MessageBus}, which defines client-specific functionalities.
 *
 * @author Mike Brock
 */
public interface ClientMessageBus extends MessageBus {
  public static final String REMOTE_QUEUE_ID_HEADER = "RemoteQueueID";

  /**
   * Retrieve all registrations that have occured during the current capture context.
   * <p/>
   * The Map returned has the subject of the registrations as the key, and Sets of registration objects as the
   * value of the Map.  The implementation of the registration objects is subject to the underlying bus
   * implementation.
   *
   * @return A map of registrations captured in the current capture context.
   */
  public Map<String, List<Object>> getCapturedRegistrations();


  /**
   * Mark the beginning of a new capture context.<p/>  From the point this message is called forward, all
   * registration events which occur will be captured.
   */
  public void beginCapture();

  /**
   * End the current capturing context.
   */
  public void endCapture();

  /**
   * Add a {@link Runnable} initialization task to be executed after the bus has successfuly finished it's
   * initialization and is now communicating with the remote bus.
   *
   * @param run a {@link Runnable} task.
   */
  public void addPostInitTask(Runnable run);

  /**
   * Adds a {@link SessionExpirationListener} to this bus instance. 
   * 
   * @param listener  listener to add, must not be null
   */
  public void addSessionExpirationListener(SessionExpirationListener listener);

  /**
   * Adds a {@link PreInitializationListener} to this bus instance. It will be notified before the bus
   * initialization starts the first time and on each subsequent reconnect.
   * 
   * @param listener  listener to add, must not be null
   */
  public void addPreInitializationListener(PreInitializationListener listener);

  //public void voteForInit();

  public void init();

  public void stop(boolean sendDisconnectToServer);

  /**
   * Returns true if the bus has successfully initialized and can relay messages.
   *
   * @return boolean indicating if bus is initialized.
   */
  public boolean isInitialized();

  public void setLogAdapter(LogAdapter logAdapter);

  public Set<String> getAllRegisteredSubjects();

  public LogAdapter getLogAdapter();
}
