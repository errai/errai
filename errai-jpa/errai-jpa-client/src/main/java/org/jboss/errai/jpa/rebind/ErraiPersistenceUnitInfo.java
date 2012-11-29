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
    return getClass().getClassLoader().getResource("");
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
