/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.service;

/**
 * @author Mike Brock
 */
public abstract class AbstractErraiConfig {
  protected final String attributeName;
  protected final String defaultValue;


  private AbstractErraiConfig(String attributeName) {
    this(attributeName, null);
  }

  AbstractErraiConfig(String attributeName, String defaultValue) {
    this.attributeName = attributeName;
    this.defaultValue = defaultValue;
  }

  public boolean getBoolean(final ErraiServiceConfigurator configurator) {
    setDefaultValue(configurator);
    return configurator.getBooleanProperty(getAttributeName());
  }

  public Integer getInt(final ErraiServiceConfigurator configurator) {
    setDefaultValue(configurator);
    return configurator.getIntProperty(getAttributeName());
  }

  public String get(final ErraiServiceConfigurator configurator) {
    setDefaultValue(configurator);
    return configurator.getProperty(getAttributeName());
  }

  public void set(final ErraiServiceConfigurator configurator, final String value) {
    configurator.setProperty(getAttributeName(), value);
  }

  private void setDefaultValue(ErraiServiceConfigurator configurator) {
    if (defaultValue != null && !configurator.hasProperty(getAttributeName())) {
      configurator.setProperty(getAttributeName(), defaultValue);
    }
  }

  public String getAttributeName() {
    return attributeName;
  }
}
