/*
 * Copyright 2012 JBoss, a division of Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.databinding.client;

import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;

/**
 * Implementations are a source of {@link PropertyChangeEvent}s.
 * 
 * @author David Cracauer <dcracauer@gmail.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface HasPropertyChangeHandlers {

  /**
   * Adds a {@link PropertyChangeHandler} that will be notified when any property of the underlying
   * object changes. Multiple handlers can be registered. If the same handler instance is passed
   * multiple times, it will be notified multiple times.
   * 
   * @param handler
   *          The {@link PropertyChangeHandler} instance, must not be null.
   */
  public void addPropertyChangeHandler(PropertyChangeHandler<?> handler);

  /**
   * Removes a {@link PropertyChangeHandler}, previously registered by a call to
   * {@link #addPropertyChangeHandler(PropertyChangeHandler)}. If the handler was added more than
   * once to the same event source, it will be notified one less time after being removed. If the
   * provided handler is null, or was never added, no exception is thrown and no action is taken.
   * 
   * @param handler
   *          the {@link PropertyChangeHandler} instance
   */
  public void removePropertyChangeHandler(PropertyChangeHandler<?> handler);

  /**
   * Adds a {@link PropertyChangeHandler} that will be notified when the given property of the
   * underlying object changes. Multiple handlers can be registered. If the same handler instance is
   * passed multiple times, it will be notified multiple times.
   * 
   * @param property
   *          The name of the property or a property chain (e.g. customer.address.street) to receive
   *          events for. A property expression can end in a wildcard to indicate that changes of
   *          any property of the corresponding bean should be observed (e.g customer.address.*). A
   *          double wildcard can be used at the end of a property expression to register a
   *          cascading change handler for any nested property (e.g customer.**). Must not be null.
   * @param handler
   *          The {@link PropertyChangeHandler} instance that should receive the events. Must not be
   *          null.
   */
  public <T> void addPropertyChangeHandler(String property, PropertyChangeHandler<T> handler);

  /**
   * Removes a {@link PropertyChangeHandler}, previously registered by a call to
   * {@link #addPropertyChangeHandler(String, PropertyChangeHandler)} for the same property name. If
   * the handler was added more than once to the same event source and property name, it will be
   * notified one less time after being removed. If the provided handler is null, or was never added
   * for the given property, no exception is thrown and no action is taken.
   * 
   * @param property
   *          The name of the property or a property chain expression (e.g.
   *          customer.address.street). Must not be null.
   * @param handler
   *          the {@link PropertyChangeHandler} instance
   */
  public void removePropertyChangeHandler(String property, PropertyChangeHandler<?> handler);

}
