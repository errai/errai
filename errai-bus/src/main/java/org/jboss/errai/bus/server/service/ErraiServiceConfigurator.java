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

package org.jboss.errai.bus.server.service;

import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.metadata.MetaDataScanner;

import java.util.Map;

/**
 * The <tt>ErraiServiceConfigurator</tt> is a template for creating a configuration for a service
 */
public interface ErraiServiceConfigurator {
  /**
   * Provides access to the {@link org.jboss.errai.common.metadata.MetaDataScanner}
   * that is used to read component annotation meta data.
   *
   * @return
   */
  public MetaDataScanner getMetaDataScanner();

  /**
   * Gets the resource providers associated with this configurator
   *
   * @return the resource providers associated with this configurator
   */
  public Map<String, ResourceProvider> getResourceProviders();

  /**
   * Gets the resources attached to the specified resource class
   *
   * @param resourceClass - the class to search the resources for
   * @param <T>           - the class type
   * @return the resource of type <tt>T</tt>
   */
  public <T> T getResource(Class<? extends T> resourceClass);

  /**
   * Returns true if the configuration has this <tt>key</tt> property
   *
   * @param key - the property too search for
   * @return false if the property does not exist
   */
  public boolean hasProperty(String key);

  /**
   * Gets the property associated with the key
   *
   * @param key - the key to search for
   * @return the property, if it exists, null otherwise
   */
  public String getProperty(String key);

  /**
   * Gets the property associated with the key. Returns true if the flag is set true, or false if not or
   * if the property is undefined.
   * @param key
   * @return
   */
  public boolean getBooleanProperty(String key);

  /**
   * Gets the property associated with the key. Returns the number if set, or null if not set. Throws
   * a NumberFormatException if the underlying key is not a number.
   * @param key
   * @return
   */
  public Integer getIntProperty(String key);

  /**
   * Sets a property with the specified key and value
   *
   * @param key the name of the property.
   * @param value the string value of the property.
   */
  public void setProperty(String key, String value);
}



