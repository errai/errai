package org.jboss.errai.forge.config;

import java.io.File;

/**
 * An enumeration of project properties stored in a {@link ProjectConfig}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public enum ProjectProperty {
  MODULE_FILE(File.class),
  MODULE_LOGICAL(String.class),
  /*
   * This can be different than the logical name if the module uses the
   * "rename-to" attribute.
   */
  MODULE_NAME(String.class),
  INSTALLED_FEATURES(SerializableSet.class),
  ERRAI_VERSION(String.class),
  CORE_IS_INSTALLED(Boolean.class);

  public final Class<?> valueType;

  private ProjectProperty(Class<?> type) {
    valueType = type;
  }
}