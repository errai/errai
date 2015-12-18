/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.config;

import org.jboss.errai.forge.config.converter.ConfigTypeConverter;
import org.jboss.errai.forge.config.converter.ConfigTypeConverterFactory;
import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.configuration.facets.ConfigurationFacet;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * A singleton class for accessing project-wide plugin settings.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ConfigurationFacet.class })
public class ProjectConfig extends AbstractFacet<Project> implements ProjectFacet {

  public static final String PREFIX = "ERRAI_FORGE_";

  private Project project;
  
  @Inject
  private ConfigTypeConverterFactory converterFactory;

  @Override
  public Project getFaceted() {
    return project;
  }

  @Override
  public void setFaceted(final Project origin) {
    project = origin;
  }

  /**
   * Get the value of a {@link ProjectProperty}.
   * 
   * @param property
   *          The {@link ProjectProperty} to which the returned value belongs.
   * @param type
   *          The type of the returned value.
   * @return The value associated with the given {@link ProjectProperty}, or
   *         null if none exists.
   */
  public <T> T getProjectProperty(final ProjectProperty property, final Class<T> type) {
    final ConfigurationFacet config = project.getFacet(ConfigurationFacet.class);
    final Object rawPropertyValue = config.getConfiguration().getProperty(getProjectAttribute(property));

    if (rawPropertyValue != null) {
      if (!type.equals(property.valueType))
        throw new RuntimeException(String.format("Expected type %s for property %s. Found type %s.",
                property.valueType, property.name(), type));
      
      final ConfigTypeConverter<T> converter = converterFactory.getConverter(type);

      return (T) converter.convertFromString(rawPropertyValue.toString());
    }
    else {
      return null;
    }
  }

  /**
   * Set the value of a {@link ProjectProperty}. This value will persist in the
   * forge configurations.
   * 
   * @param property
   *          The {@link ProjectProperty} to which a value will be assigned.
   * @param value
   *          The value to assign to the property.
   * @throws IllegalArgumentException
   *           If the class of the {@code value} does not match
   *           {@code property.valueType}.
   */
  public <T> void setProjectProperty(ProjectProperty property, T value) {
    if (!property.valueType.isInstance(value)) {
      throw new IllegalArgumentException("Value for property " + property.toString() + " must be type "
              + property.valueType + ", not " + value.getClass());
    }

    @SuppressWarnings("unchecked")
    final ConfigTypeConverter<T> converter = (ConfigTypeConverter<T>) converterFactory.getConverter(property.valueType);
    final Configuration config = project.getFacet(ConfigurationFacet.class).getConfiguration();
    final String attribute = getProjectAttribute(property);

    config.setProperty(attribute, converter.convertToString(value));
  }

  /**
   * @return The name used to store and retrieve persistent project
   *         configurations.
   */
  public static String getProjectAttribute(final ProjectProperty prop) {
    return PREFIX + prop.name();
  }

  @Override
  public boolean install() {
    return true;
  }

  @Override
  public boolean isInstalled() {
    return project != null && project.hasFacet(ConfigurationFacet.class);
  }

  @Override
  public boolean uninstall() {
    if (isInstalled()) {
      final ConfigurationFacet configFacet = project.getFacet(ConfigurationFacet.class);
      final Configuration config = configFacet.getConfiguration();

      for (final ProjectProperty property : Arrays.asList(ProjectProperty.values())) {
        if (config.containsKey(getProjectAttribute(property))) {
          config.clearProperty(getProjectAttribute(property));
        }
      }

      return true;
    }
    else {
      return false;
    }
  }
}
