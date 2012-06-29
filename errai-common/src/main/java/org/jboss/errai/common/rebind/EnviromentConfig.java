package org.jboss.errai.common.rebind;

import org.jboss.errai.codegen.meta.MetaClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class EnviromentConfig {
  private final Map<String, String> mappingAliases;
  private final Set<MetaClass> exposedClasses;
  private final Set<MetaClass> portableSuperTypes;
  private final Map<String, String> frameworkProperties;

  EnviromentConfig(final Map<String, String> mappingAliases,
                   final Set<MetaClass> exposedClasses,
                   final Set<MetaClass> portableSuperTypes,
                   final Map<String, String> frameworkProperties) {
    this.mappingAliases = Collections.unmodifiableMap(mappingAliases);
    this.exposedClasses = Collections.unmodifiableSet(exposedClasses);
    this.portableSuperTypes = Collections.unmodifiableSet(portableSuperTypes);
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

  public Map<String, String> getFrameworkProperties() {
    return frameworkProperties;
  }
}

