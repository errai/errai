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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;

/**
 * This is a utility class that can be used by implementations of {@link HasPropertyChangeHandlers}. It manages a list
 * of handlers and dispatches {@link PropertyChangeEvent}s.
 * 
 * @author David Cracauer <dcracauer@gmail.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PropertyChangeHandlerSupport {

  private final List<PropertyChangeHandler> handlers = new ArrayList<PropertyChangeHandler>();

  /**
   * Adds a {@link PropertyChangeHandler} to the list of handlers. Multiple handlers can be registered. If the same
   * handler instance is passed multiple times, it will be notified multiple times.
   * 
   * @param handler
   *          The {@link PropertyChangeHandler} to add, must not be null.
   */
  public void addPropertyChangeHandler(PropertyChangeHandler handler) {
    handlers.add(Assert.notNull(handler));
  }

  /**
   * Removes a {@link PropertyChangeHandler} from the list of handlers. If the handler was added more than once to the
   * same event source, it will be notified one less time after being removed. If listener is null, or was never added,
   * no exception is thrown and no action is taken.
   * 
   * @param handler
   *          The {@link PropertyChangeHandler} to remove.
   */
  public void removePropertyChangeHandler(PropertyChangeHandler handler) {
    handlers.remove(handler);
  }

  /**
   * Notify registered {@link PropertyChangeHandlers} of a {@link PropertyChangeEvent}. Will only dispatch events that
   * represent a change. If oldValue and newValue are equal, the event will be ignored.
   * 
   * @param event
   *          {@link the PropertyChangeEvent} to provide to handlers.
   */
  public void notifyHandlers(PropertyChangeEvent event) {
    if (!acceptEvent(event)) {
      return;
    }

    for (PropertyChangeHandler handler : handlers) {
      handler.onPropertyChange(event);
    }
  }

  private boolean acceptEvent(PropertyChangeEvent event) {
    if (event == null) {
      return false;
    }

    if (event.getOldValue() == null) {
      return event.getNewValue() != null;
    }

    if (event.getNewValue() == null) {
      return event.getOldValue() != null;
    }

    return !event.getOldValue().equals(event.getNewValue());
  }
}