package org.jboss.errai.common.rebind;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class EnviromentConfig {
  private final Map<String, String> mappingAliases;
  private final Set<Class<?>> exposedClasses;

  EnviromentConfig(Map<String, String> mappingAliases, Set<Class<?>> exposedClasses) {
    this.mappingAliases = Collections.unmodifiableMap(mappingAliases);
    this.exposedClasses = Collections.unmodifiableSet(exposedClasses);
  }

  public Map<String, String> getMappingAliases() {
    return mappingAliases;
  }

  public Set<Class<?>> getExposedClasses() {
    return exposedClasses;
  }
}

