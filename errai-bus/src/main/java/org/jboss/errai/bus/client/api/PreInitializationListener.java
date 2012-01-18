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

package org.jboss.errai.bus.client.api;

/**
 * Callback interface that is notified before the client bus begins its
 * initialization tasks. Also called after the server becomes unreachable, after
 * {@link SessionExpirationListener SessionExpirationListeners} are notified
 * that the previous session has expired, but before each time the bus begins an
 * attempt to reconnect to the server and establish a new session.
 * <p>
 * This callback is primarily of use to framework-style extensions to Errai, and
 * is not expected to be of great value when creating applications directly on
 * top of Errai.
 *
 * @see SessionExpirationListener
 *
 * @author Mike Brock
 */
public interface PreInitializationListener {

  /**
   * Called by the client message bus before it attempts communication with the
   * server, or after server connectivity has been lost and before a
   * reconnection attempt is made.
   */
  public void beforeInitialization();
}
