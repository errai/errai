package org.jboss.errai.common.rebind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class EnviromentConfig {
  private final Map<String, String> mappingAliases;
  private final Set<Class<?>> exposedClasses;
  private final Set<Class<?>> portableSuperTypes;
  private final Map<String, String> frameworkProperties;

  EnviromentConfig(final Map<String, String> mappingAliases,
                   final Set<Class<?>> exposedClasses,
                   final Set<Class<?>> portableSuperTypes,
                   final Map<String, String> frameworkProperties) {
    this.mappingAliases = Collections.unmodifiableMap(mappingAliases);
    this.exposedClasses = Collections.unmodifiableSet(exposedClasses);
    this.portableSuperTypes = Collections.unmodifiableSet(portableSuperTypes);
    this.frameworkProperties = new HashMap<String, String>(frameworkProperties);
  }

  public Map<String, String> getMappingAliases() {
    return mappingAliases;
  }

  public Set<Class<?>> getExposedClasses() {
    return exposedClasses;
  }

  public Set<Class<?>> getPortableSuperTypes() {
    return portableSuperTypes;
  }

  public Map<String, String> getFrameworkProperties() {
    return frameworkProperties;
  }
}

