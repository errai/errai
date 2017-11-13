/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.config.propertiesfile;

import org.jboss.errai.config.ErraiAppConfiguration;
import org.jboss.errai.config.rebind.EnvUtil;

import java.util.Map;
import java.util.Optional;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAppPropertiesErraiAppConfiguration implements ErraiAppConfiguration {

  public static final String ERRAI_IOC_ASYNC_BEAN_MANAGER = "errai.ioc.async_bean_manager";
  public static final String FORCE_STATIC_MARSHALLERS = "errai.marshalling.force_static_marshallers";
  public static final String USE_STATIC_MARSHALLERS = "errai.marshalling.use_static_marshallers";
  private static final String JS_INTEROP_SUPPORT_ENABLED = "errai.ioc.jsinterop.support";

  @Override
  public boolean isUserEnabledOnHostPage() {
    return false; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public boolean isWebSocketServerEnabled() {
    return false; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public String getApplicationContext() {
    return null; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public boolean jsInteropSupportEnabled() {
    return Boolean.getBoolean(JS_INTEROP_SUPPORT_ENABLED);
  }

  @Override
  public boolean isAutoDiscoverServicesEnabled() {
    return false; //FIXME: Implement when migrating owner module to APT generators
  }

  @Override
  public boolean asyncBeanManager() {
    final String s = ErraiAppPropertiesConfigurationUtil.getEnvironmentConfig()
            .getFrameworkOrSystemProperty(ERRAI_IOC_ASYNC_BEAN_MANAGER);
    return s != null && Boolean.parseBoolean(s);
  }

  @Override
  public boolean isAptEnvironment() {
    return false;
  }

  @Override
  public Optional<String> custom(final String propertyName) {
    return Optional.ofNullable(
            ErraiAppPropertiesConfigurationUtil.getEnvironmentConfig().getFrameworkOrSystemProperty(propertyName));
  }

  @Override
  public boolean forceStaticMarshallers() {
    final Map<String, String> frameworkProperties = ErraiAppPropertiesConfigurationUtil.getEnvironmentConfig()
            .getFrameworkProperties();
    if (frameworkProperties.containsKey(FORCE_STATIC_MARSHALLERS)) {
      return "true".equals(frameworkProperties.get(FORCE_STATIC_MARSHALLERS));
    } else {
      return false;
    }
  }

  @Override
  public boolean useStaticMarshallers() {
    final Map<String, String> frameworkProperties = ErraiAppPropertiesConfigurationUtil.getEnvironmentConfig()
            .getFrameworkProperties();
    if (frameworkProperties.containsKey(ErraiAppPropertiesErraiAppConfiguration.USE_STATIC_MARSHALLERS)) {
      return "true".equals(frameworkProperties.get(ErraiAppPropertiesErraiAppConfiguration.USE_STATIC_MARSHALLERS));
    } else {
      return !EnvUtil.isDevMode();
    }
  }

  @Override
  public boolean lazyLoadBuiltinMarshallers() {
    return CommonConfigAttributes.LAZY_LOAD_BUILTIN_MARSHALLERS.getBoolean();
  }

  @Override
  public boolean makeDefaultArrayMarshallers() {
    return CommonConfigAttributes.MAKE_DEFAULT_ARRAY_MARSHALLERS.getBoolean();
  }

  public enum CommonConfigAttributes {

    LAZY_LOAD_BUILTIN_MARSHALLERS("errai.marshalling.lazy_load_builtin_marshallers", "true"),

    MAKE_DEFAULT_ARRAY_MARSHALLERS("errai.marshalling.make_default_array_marshallers", "false");

    protected final String attributeName;
    protected final String defaultValue;

    CommonConfigAttributes(final String attributeName, final String defaultValue) {
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
      return ErraiAppPropertiesConfigurationUtil.getEnvironmentConfig().getFrameworkProperties();
    }

    public String getAttributeName() {
      return attributeName;
    }
  }
}
