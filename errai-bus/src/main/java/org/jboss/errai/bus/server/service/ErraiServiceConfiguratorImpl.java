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

import com.google.inject.Inject;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.ResourceBundle.getBundle;

/**
 * Default implementation of the ErraiBus server-side configurator.
 */
public class ErraiServiceConfiguratorImpl implements ErraiServiceConfigurator {
  private MetaDataScanner scanner;
  private Map<String, String> properties;

  private final Map<String, String> attributeMap;
  private Map<Class<?>, ResourceProvider> extensionBindings;
  private Map<String, ResourceProvider> resourceProviders;
  private Set<Class> serializableTypes;

  /**
   * Initializes the <tt>ErraiServiceConfigurator</tt> with a specified <tt>ServerMessageBus</tt>
   */
  @Inject
  public ErraiServiceConfiguratorImpl() {
    this.attributeMap = new HashMap<String, String>();
    this.extensionBindings = new HashMap<Class<?>, ResourceProvider>();
    this.resourceProviders = new HashMap<String, ResourceProvider>();
    this.serializableTypes = new HashSet<Class>();
    this.scanner = ScannerSingleton.getOrCreateInstance();
    loadServiceProperties();
  }

  // lockdown the configuration so it can't be modified.
  public void lockdown() {
//    properties = Collections.unmodifiableMap(properties);
    extensionBindings = Collections.unmodifiableMap(extensionBindings);
    serializableTypes = Collections.unmodifiableSet(serializableTypes);
  }

  private void loadServiceProperties() {
    properties = new HashMap<String, String>();
    String bundlePath = System.getProperty("errai.service_config_prefix_path");

    try {
      ResourceBundle erraiServiceConfig = getBundle(bundlePath == null ? "ErraiService" : bundlePath + ".ErraiService");
      Enumeration<String> keys = erraiServiceConfig.getKeys();
      String key;
      while (keys.hasMoreElements()) {
        key = keys.nextElement();
        properties.put(key, erraiServiceConfig.getString(key));
      }
    }
    catch (Exception e) {
      if (bundlePath == null) {
        // try to load the default service bundle -- used for testing, etc.
        System.setProperty("errai.service_config_prefix_path", "org.jboss.errai.bus");
        loadServiceProperties();
      }
    }
  }

  public MetaDataScanner getMetaDataScanner() {
    return scanner;
  }

  /**
   * Gets the resource providers associated with this configurator
   *
   * @return the resource providers associated with this configurator
   */
  public Map<String, ResourceProvider> getResourceProviders() {
    return this.resourceProviders;
  }

  /**
   * Returns true if the configuration has this <tt>key</tt> property
   *
   * @param key
   *     - the property too search for
   *
   * @return false if the property does not exist
   */
  public boolean hasProperty(String key) {
    return properties.containsKey(key) || System.getProperty(key) != null;
  }

  /**
   * Gets the property associated with the key
   *
   * @param key
   *     - the key to search for
   *
   * @return the property, if it exists, null otherwise
   */
  public String getProperty(String key) {
    if (properties.containsKey(key)) {
      return properties.get(key);
    }
    else{
      return System.getProperty(key);
    }
  }

  @Override
  public boolean getBooleanProperty(String key) {
    return hasProperty(key) && "true".equals(getProperty(key));
  }


  @Override
  public Integer getIntProperty(String key) {
    if (hasProperty(key)) {
      String val = getProperty(key);
      try {
        return Integer.parseInt(val);
      }
      catch (NumberFormatException e) {
        throw new RuntimeException("expected an integer value for key '" + key + "': but got: "
            + val);
      }
    }
    return null;
  }

  /**
   * Gets the resources attached to the specified resource class
   *
   * @param resourceClass
   *     - the class to search the resources for
   * @param <T>
   *     - the class type
   *
   * @return the resource of type <tt>T</tt>
   */
  @SuppressWarnings({"unchecked"})
  public <T> T getResource(Class<? extends T> resourceClass) {
    if (extensionBindings.containsKey(resourceClass)) {
      return (T) extensionBindings.get(resourceClass).get();
    }
    else {
      return null;
    }
  }

  public Map<Class<?>, ResourceProvider> getExtensionBindings() {
    return extensionBindings;
  }

  public Set<Class> getSerializableTypes() {
    return serializableTypes;
  }

  @Override
  public void setProperty(String key, String value) {
    this.properties.put(key, value);
  }
}

