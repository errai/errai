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

package org.jboss.errai.bus.client.api;

import java.util.Collection;

/**
 * The combination of a communication channel (identified by an HTTPSession or other communication session, such as an
 * open WebSocket channel) with an Errai Bus queue ID. An instance of QueueSession identifies a physical communication
 * link with a specific subject on a remote bus. A single communication link typically has many QueueSession instances
 * (one per bus subject).
 * 
 * <h3>Note on scope of attributes</h3>
 * <p>
 * QueueSession provides a map-like facility for storing and retrieving arbitrary object ("attributes") by name. These
 * attributes are shared among all QueueSession instances that are associated with the same communication channel (for
 * example, an HTTPSession or a WebSocketChannel).
 */
public interface QueueSession {

  /**
   * Returns the ID of this session, which uniquely identifies it within the scope of this client (or server for a
   * server side session). This is <i>not</i> the ID of the underlying wrapped session (for example, it is not a Servlet
   * Session ID).
   * 
   * @return the session id, which is unique within the confines of the present client (or server). Never null.
   */
  public String getSessionId();

  /**
   * Returns the ID of the session this QueueSession wraps. In the case of a wrapper around an HTTP Session, this method
   * would return the HTTP Session ID.
   * 
   * @return the session ID from the underlying communication layer, or a unique ID synthesized by the framework for
   *         communication layers that don't have unique session ID's of their own. Never null.
   */
  public String getParentSessionId();

  /**
   * Closes this session and notifies the {@link SessionEndListener}s (optional operation; not all QueueSession
   * implementations are closeable).
   * 
   * @return true, if this session was closed and listeners have been notified. In the case of a QueueSession that does
   *         not implement this operation, listeners are not notified and this method returns false.
   */
  public boolean endSession();

  /**
   * Associates the given value with the given key, replacing the existing value, if any, for the key.
   * <p>
   * See the class-level documentation for a note on the scope of these attributes.
   * 
   * @param attribute
   *          the name (key) of the attribute. Not null.
   * @param value
   *          new value for attribute. Null is permitted.
   */
  public void setAttribute(String attribute, Object value);

  /**
   * Returns the value associated with the given key.
   * <p>
   * See the class-level documentation for a note on the scope of these attributes.
   * 
   * @param type
   *          the type to attempt to cast the attribute's value to
   * @param attribute
   *          the name (key) of the attribute. Not null.
   * @param <T>
   *          the type
   * @return the value of the attribute, as the specified type. Returns null if the attribute's value is null, or if no
   *         such attribute exists. Use {@link #hasAttribute(String)} to test for the existence of a null-valued
   *         attribute.
   */
  public <T> T getAttribute(Class<T> type, String attribute);

  /**
   * Returns the names of all attributes within this session.
   * <p>
   * See the class-level documentation for a note on the scope of these attributes.
   * 
   * @return collection of attribute names.
   */
  public Collection<String> getAttributeNames();

  /**
   * Returns true if the specified attribute exists.
   * <p>
   * See the class-level documentation for a note on the scope of these attributes.
   * 
   * @param attribute
   *          the attribute name to search for.
   * @return true if it exists, otherwise false
   */
  public boolean hasAttribute(String attribute);

  /**
   * Removes the specified attribute from this session.
   * <p>
   * See the class-level documentation for a note on the scope of these attributes.
   * 
   * @param attribute
   *          the name of the attribute to remove.
   * @return the attribute value previously associated with the given name (which may be null). Returns null in case the
   *         attribute did not exist.
   */
  public Object removeAttribute(String attribute);

  /**
   * Registers a listener that will notified when this session ends.
   * 
   * @param listener
   *          the listener to be notified at session end.
   */
  public void addSessionEndListener(SessionEndListener listener);

  public boolean isValid();
}
