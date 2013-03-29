package org.jboss.errai.config.rebind;

import java.util.Map;

/**
 * @author Mike Brock
 */
public enum CommonConfigAttribs {
  LAZY_LOAD_BUILTIN_MARSHALLERS("errai.marshalling.lazy_load_builtin_marshallers", "true"),
  MAKE_DEFAULT_ARRAY_MARSHALLERS("errai.marshalling.make_default_array_marshallers", "false");

  protected final String attributeName;
  protected final String defaultValue;

  CommonConfigAttribs(final String attributeName, final String defaultValue) {
    this.attributeName = attributeName;
    this.defaultValue = defaultValue;
  }

  public boolean getBoolean() {
    setDefaultValue();
    return Boolean.parseBoolean(getConfigMap().get(getAttributeName()));
  }

  public Integer getInt() {
    setDefaultValue();
    return Integer.parseInt(getConfigMap().get(getAttributeName()));
  }

  public String get() {
    setDefaultValue();
    return getConfigMap().get(getAttributeName());
  }

  public void set(final String value) {
    getConfigMap().put(getAttributeName(), value);
  }

  private void setDefaultValue() {
    final Map<String, String> map = getConfigMap();
    if (defaultValue != null && !map.containsKey(getAttributeName())) {
      map.put(getAttributeName(), defaultValue);
    }
  }

  private static Map<String, String> getConfigMap() {
    return EnvUtil.getEnvironmentConfig().getFrameworkProperties();
  }

  public String getAttributeName() {
    return attributeName;
  }
  }
