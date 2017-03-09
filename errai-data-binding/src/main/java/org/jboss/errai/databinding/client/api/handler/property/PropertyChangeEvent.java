/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client.api.handler.property;

import java.util.Objects;

/**
 * Dispatched when a bound property has changed.
 *
 * @author David Cracauer <dcracauer@gmail.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PropertyChangeEvent<T> {
  private final Object source;
  private final String propertyName;
  private final T oldValue;
  private final T newValue;

  public PropertyChangeEvent(final Object source, final String propertyName, final T oldValue, final T newValue) {
    this.source = source;
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Gets the new value of the property.
   *
   * @return new property value.
   */
  public T getNewValue() {
    return newValue;
  }

  /**
   * Gets the old value of the property.
   *
   * @return old property value.
   */
  public T getOldValue() {
    return oldValue;
  }

  /**
   * Gets the property name.
   *
   * @return the property name.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Gets the object on which the Event initially occurred.
   *
   * @return the source object.
   */
  public Object getSource() {
    return source;
  }

  @Override
  public String toString() {
    return "[property=" + propertyName + ", source=" + source.toString() + ", oldValue=" + oldValue + ", newValue="
            + newValue + "]";
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof PropertyChangeEvent) {
      final PropertyChangeEvent<?> other = (PropertyChangeEvent<?>) obj;
      return Objects.equals(other.source, source)
              && Objects.equals(other.propertyName, propertyName)
              && Objects.equals(other.oldValue, oldValue)
              && Objects.equals(other.newValue, newValue);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, propertyName, oldValue, newValue);
  }
}
