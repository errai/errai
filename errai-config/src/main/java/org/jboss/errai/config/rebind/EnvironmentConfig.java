package org.jboss.errai.config.rebind;

import org.jboss.errai.codegen.meta.MetaClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class EnvironmentConfig {
  private final Map<String, String> mappingAliases;
  private final Set<MetaClass> exposedClasses;
  private final Set<MetaClass> portableSuperTypes;
  private final Map<String, String> frameworkProperties;

  private final Set<String> explicitTypes;

  EnvironmentConfig(final Map<String, String> mappingAliases,
                    final Set<MetaClass> exposedClasses,
                    final Set<MetaClass> portableSuperTypes,
                    final Set<String> explicitTypes,
                    final Map<String, String> frameworkProperties) {
    this.mappingAliases = Collections.unmodifiableMap(mappingAliases);
    this.exposedClasses = Collections.unmodifiableSet(exposedClasses);
    this.portableSuperTypes = Collections.unmodifiableSet(portableSuperTypes);
    this.explicitTypes = Collections.unmodifiableSet(explicitTypes);
    this.frameworkProperties = new HashMap<String, String>(frameworkProperties);
  }

  public Map<String, String> getMappingAliases() {
    return mappingAliases;
  }

  public Set<MetaClass> getExposedClasses() {
    return exposedClasses;
  }

  public Set<MetaClass> getPortableSuperTypes() {
    return portableSuperTypes;
  }

  public Set<String> getExplicitTypes() {
    return explicitTypes;
  }

  public Map<String, String> getFrameworkProperties() {
    return frameworkProperties;
  }

  public String getFrameworkOrSystemProperty(String property) {
    final String value = frameworkProperties.get(property);
    if (value == null) {
      return System.getProperty(property);
    }
    else {
      return value;
    }
  }
}

