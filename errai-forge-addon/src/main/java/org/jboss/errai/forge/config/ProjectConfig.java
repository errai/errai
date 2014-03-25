package org.jboss.errai.forge.config;

import java.io.File;
import java.util.Arrays;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.configuration.facets.ConfigurationFacet;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;

/**
 * A singleton class for accessing project-wide plugin settings.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ConfigurationFacet.class })
public class ProjectConfig extends AbstractFacet<Project> implements ProjectFacet {

  /**
   * An enumeration of project properties stored in a {@link ProjectConfig}.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static enum ProjectProperty {
    MODULE_FILE(File.class),
    MODULE_LOGICAL(String.class),
    /*
     * This can be different than the logical name if the module uses the
     * "rename-to" attribute.
     */
    MODULE_NAME(String.class),
    INSTALLED_FEATURES(SerializableSet.class),
    ERRAI_VERSION(String.class);

    /**
     * The type of value stored by this property.
     */
    public final Class<?> valueType;

    private ProjectProperty(Class<?> type) {
      valueType = type;
    }
  }

  public static final String PREFIX = "errai-forge-";

  private Project project;

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
  @SuppressWarnings("unchecked")
  public <T> T getProjectProperty(final ProjectProperty property, final Class<T> type) {
    final ConfigurationFacet config = project.getFacet(ConfigurationFacet.class);
    final Object rawPropertyValue = config.getConfiguration().getProperty(getProjectAttribute(property));

    if (rawPropertyValue != null) {
      if (!type.equals(property.valueType))
        throw new RuntimeException(String.format("Expected type %s for property %s. Found type %s.",
                property.valueType, property.name(), type));
      
      // Special cases
      if (type.equals(File.class)) {
        return (T) new File(rawPropertyValue.toString());
      }
      else if (type.equals(SerializableSet.class)) {
        return (T) SerializableSet.deserialize(rawPropertyValue.toString());
      }
      else {
        return (T) rawPropertyValue;
      }
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

    final Configuration config = project.getFacet(ConfigurationFacet.class).getConfiguration();
    if (property.valueType.equals(File.class)) {
      config.setProperty(getProjectAttribute(property), File.class.cast(value).getAbsolutePath());
    }
    else if (property.valueType.equals(SerializableSet.class)) {
      config.setProperty(getProjectAttribute(property), SerializableSet.class.cast(value).serialize());
    }
    else {
      config.setProperty(getProjectAttribute(property), value);
    }
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
