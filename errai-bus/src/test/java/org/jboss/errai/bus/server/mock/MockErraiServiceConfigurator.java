/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.mock;

import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;

import java.util.Collections;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class MockErraiServiceConfigurator implements ErraiServiceConfigurator {
  @Override
  public MetaDataScanner getMetaDataScanner() {
    return ScannerSingleton.getOrCreateInstance();
  }

  @Override
  public Map<String, ResourceProvider> getResourceProviders() {
    return Collections.emptyMap();
  }

  @Override
  public <T> T getResource(Class<? extends T> resourceClass) {
    return null;
  }

  @Override
  public boolean hasProperty(String key) {
    return false;
  }

  @Override
  public String getProperty(String key) {
    return null;
  }

  @Override
  public boolean getBooleanProperty(String key) {
    return false;
  }

  @Override
  public Integer getIntProperty(String key) {
    return null;
  }

  @Override
  public void setProperty(String key, String value) {
  }
}
