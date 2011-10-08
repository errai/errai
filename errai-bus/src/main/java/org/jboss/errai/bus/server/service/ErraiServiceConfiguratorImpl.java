/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.service;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;

import java.util.*;

import static java.util.ResourceBundle.getBundle;

/**
 * Default implementation of the ErraiBus server-side configurator.
 */
public class ErraiServiceConfiguratorImpl implements ErraiServiceConfigurator {

  private ServerMessageBus bus;
  private MetaDataScanner scanner;
  private Map<String, String> properties;

  private Map<Class<?>, ResourceProvider> extensionBindings;
  private Map<String, ResourceProvider> resourceProviders;
  private Set<Class> serializableTypes;


  /**
   * Initializes the <tt>ErraiServiceConfigurator</tt> with a specified <tt>ServerMessageBus</tt>
   *
   * @param bus - the server message bus in charge of transmitting messages
   */
  @Inject
  public ErraiServiceConfiguratorImpl(ServerMessageBus bus) {
    this.bus = bus;
    this.extensionBindings = new HashMap<Class<?>, ResourceProvider>();
    this.resourceProviders = new HashMap<String, ResourceProvider>();
    this.serializableTypes = new HashSet<Class>();
    this.scanner = ScannerSingleton.getOrCreateInstance();
    loadServiceProperties();
  }

  // lockdown the configuration so it can't be modified.

  public void lockdown() {

    properties = Collections.unmodifiableMap(properties);
    extensionBindings = Collections.unmodifiableMap(extensionBindings);
    //  resourceProviders = Collections.unmodifiableMap(resourceProviders);
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
        return;
      }

      throw new ErraiBootstrapFailure("Error reading from configuration. Did you include ErraiService.properties?", e);
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
   * @param key - the property too search for
   * @return false if the property does not exist
   */
  public boolean hasProperty(String key) {
    return properties.containsKey(key);
  }

  /**
   * Gets the property associated with the key
   *
   * @param key - the key to search for
   * @return the property, if it exists, null otherwise
   */
  public String getProperty(String key) {
    return properties.get(key);
  }

  /**
   * Gets the resources attached to the specified resource class
   *
   * @param resourceClass - the class to search the resources for
   * @param <T>           - the class type
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
}
