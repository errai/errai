/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.rebind;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.jboss.errai.common.client.api.Assert;

class ErraiPersistenceUnitInfo implements PersistenceUnitInfo {

  private final List<String> managedClassNames;

  public ErraiPersistenceUnitInfo(List<String> managedClassNames) {
    this.managedClassNames = Assert.notNull(managedClassNames);
  }

  @Override
  public String getPersistenceUnitName() {
    return "ErraiClientPersistenceUnit";
  }

  @Override
  public String getPersistenceProviderClassName() {
    return null;
  }

  @Override
  public PersistenceUnitTransactionType getTransactionType() {
    return PersistenceUnitTransactionType.RESOURCE_LOCAL;
  }

  @Override
  public DataSource getJtaDataSource() {
    return null;
  }

  @Override
  public DataSource getNonJtaDataSource() {
    return null;
  }

  @Override
  public List<String> getMappingFileNames() {
    return Collections.emptyList();
  }

  @Override
  public List<URL> getJarFileUrls() {
    return Collections.emptyList();
  }

  @Override
  public URL getPersistenceUnitRootUrl() {
    return null;
  }

  @Override
  public List<String> getManagedClassNames() {
    return managedClassNames;
  }

  @Override
  public boolean excludeUnlistedClasses() {
    return false;
  }

  @Override
  public SharedCacheMode getSharedCacheMode() {
    return SharedCacheMode.NONE;
  }

  @Override
  public ValidationMode getValidationMode() {
    return ValidationMode.NONE;
  }

  @Override
  public Properties getProperties() {
    return new Properties();
  }

  @Override
  public String getPersistenceXMLSchemaVersion() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  @Override
  public void addTransformer(ClassTransformer transformer) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public ClassLoader getNewTempClassLoader() {
    return getClassLoader();
  }

}
