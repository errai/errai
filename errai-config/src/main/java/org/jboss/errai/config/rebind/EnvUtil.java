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

package org.jboss.errai.config.rebind;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.common.metadata.ErraiAppPropertiesFiles;
import org.jboss.errai.common.rebind.CacheStore;
import org.jboss.errai.common.rebind.CacheUtil;
import org.jboss.errai.config.ErraiAppPropertiesConfiguration;
import org.jboss.errai.config.ErraiAppPropertiesErraiModulesConfiguration;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.reflections.util.SimplePackageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toCollection;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {

  private static Set<MetaClass> allSerializableTypes;

  public static class EnvironmentConfigCache implements CacheStore {
    private volatile EnvironmentConfig environmentConfig;
    private final Map<String, String> permanentProperties = new ConcurrentHashMap<>();

    public EnvironmentConfigCache() {
      clear();
    }

    @Override
    public synchronized void clear() {
      environmentConfig = newEnvironmentConfig();
      environmentConfig.getFrameworkProperties().putAll(permanentProperties);
    }

    public synchronized EnvironmentConfig get() {
      return environmentConfig;
    }

    public void addPermanentFrameworkProperty(final String name, final String value) {
      permanentProperties.put(name, value);
      environmentConfig.getFrameworkProperties().put(name, value);
    }
  }

  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";

  private static volatile Boolean _isJUnitTest;

  public static boolean isJUnitTest() {
    if (_isJUnitTest != null) return _isJUnitTest;

    for (final StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.junit.client.")
          || el.getClassName().startsWith("org.junit")) {
        return _isJUnitTest = Boolean.TRUE;
      }
    }
    return _isJUnitTest = Boolean.FALSE;
  }

  private static volatile Boolean _isDevMode;

  public static boolean isDevMode() {
    if (_isDevMode != null) return _isDevMode;

    for (final StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.dev.shell.OophmSessionHandler") ||
          el.getClassName().startsWith("com.google.gwt.dev.codeserver")) {
        return _isDevMode = Boolean.TRUE;
      }
    }
    return _isDevMode = Boolean.FALSE;
  }

  private static volatile Boolean _isProdMode;

  public static boolean isProdMode() {
    if (_isProdMode != null) return _isProdMode;

    return _isProdMode = Boolean.valueOf(!isDevMode() && !isJUnitTest());
  }

  public static void recordEnvironmentState() {
    isJUnitTest();
    isDevMode();
    isProdMode();
  }

  private static Logger log = LoggerFactory.getLogger(EnvUtil.class);

  private static EnvironmentConfig newEnvironmentConfig() {
    final Map<String, String> frameworkProps = new HashMap<>();
    final Map<String, String> mappingAliases = new HashMap<>();
    final Set<MetaClass> exposedClasses = new HashSet<>();
    final Set<MetaClass> nonportableClasses = new HashSet<>();
    final Set<MetaClass> portableNonExposed = new HashSet<>();

    nonportableClasses.addAll(ClassScanner.getTypesAnnotatedWith(NonPortable.class));
    final Set<MetaClass> exposedFromScanner = new HashSet<>(ClassScanner.getTypesAnnotatedWith(Portable.class));

    addExposedInnerClasses(exposedClasses, exposedFromScanner);
    exposedClasses.addAll(exposedFromScanner);

    processErraiAppPropertiesFiles(frameworkProps, mappingAliases, exposedClasses, nonportableClasses);
    processEnvironmentConfigExtensions(exposedClasses);

    // must do this before filling in interfaces and supertypes!
    exposedClasses.removeAll(nonportableClasses);

    for (final MetaClass cls : exposedClasses) {
      fillInInterfacesAndSuperTypes(portableNonExposed, cls);
    }

    return new EnvironmentConfig(mappingAliases, exposedClasses, portableNonExposed, frameworkProps);
  }

  private static void processEnvironmentConfigExtensions(final Set<MetaClass> exposedClasses) {
    final Collection<MetaClass> exts = ClassScanner.getTypesAnnotatedWith(EnvironmentConfigExtension.class, true);
    for (final MetaClass cls : exts) {
      try {
        final Class<? extends ExposedTypesProvider> providerClass = cls.unsafeAsClass().asSubclass(ExposedTypesProvider.class);
        for (final MetaClass exposedType : providerClass.newInstance().provideTypesToExpose()) {
          if (exposedType.isPrimitive()) {
            exposedClasses.add(exposedType.asBoxed());
          }
          else if (exposedType.isConcrete()) {
            exposedClasses.add(exposedType);
          }
        }
      }
      catch (final Throwable e) {
        throw new RuntimeException("unable to load environment extension: " + cls.getFullyQualifiedName(), e);
      }
    }
  }

  private static void processErraiAppPropertiesFiles(final Map<String, String> frameworkProps, final Map<String, String> mappingAliases,
          final Set<MetaClass> exposedClasses, final Set<MetaClass> nonportableClasses) {
    for (final URL url : getErraiAppPropertiesFilesUrls()) {
      InputStream inputStream = null;
      try {
        log.debug("checking " + url.getFile() + " for configured types ...");
        inputStream = url.openStream();

        final ResourceBundle props = new PropertyResourceBundle(inputStream);
        processErraiAppPropertiesBundle(frameworkProps, mappingAliases, exposedClasses, nonportableClasses, props);
      }
      catch (final IOException e) {
        throw new RuntimeException("error reading ErraiApp.properties", e);
      }
      finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (final IOException e) {
            //
          }
        }
      }
    }
  }

  private static void processErraiAppPropertiesBundle(final Map<String, String> frameworkProps, final Map<String, String> mappingAliases,
          final Set<MetaClass> exposedClasses, final Set<MetaClass> nonportableClasses, final ResourceBundle props) {

    for (final String key : props.keySet()) {
      final String value = props.getString(key);
      updateFrameworkProperties(frameworkProps, key, value);

      if (key.equals(ErraiAppPropertiesErraiModulesConfiguration.SERIALIZABLE_TYPES)) {
        addSerializableTypes(exposedClasses, value);
      }
      else if (key.equals(ErraiAppPropertiesErraiModulesConfiguration.NON_SERIALIZABLE_TYPES)) {
        addNonSerializableTypes(exposedClasses, nonportableClasses, value);
      }
      else if (key.equals(CONFIG_ERRAI_MAPPING_ALIASES)) {
        addMappingAliases(mappingAliases, value);
      }
    }
  }

  private static void addMappingAliases(final Map<String, String> mappingAliases, final String value) {
    for (final String s : value.split(" ")) {
      try {
        final String[] mapping = s.split("->");

        if (mapping.length != 2) {
          throw new RuntimeException("syntax error: mapping for marshalling alias: " + s);
        }

        final Class<?> fromMapping = Class.forName(mapping[0].trim());
        final Class<?> toMapping = Class.forName(mapping[1].trim());

        mappingAliases.put(fromMapping.getName(), toMapping.getName());
      }
      catch (final Exception e) {
        throw new RuntimeException("could not find class defined in ErraiApp.properties for mapping: " + s, e);
      }
    }
  }

  private static void addNonSerializableTypes(final Set<MetaClass> exposedClasses, final Set<MetaClass> nonportableClasses,
          final String value) {
    final Set<String> patterns = new LinkedHashSet<>();
    for (final String s : value.split(" ")) {
      final String singleValue = s.trim();
      if (singleValue.endsWith("*")) {
        patterns.add(singleValue);
      }
      else {
        try {
          nonportableClasses.add(MetaClassFactory.get(singleValue));
        }
        catch (final Exception e) {
          throw new RuntimeException("could not find class defined in ErraiApp.properties as nonserializable: "
                  + s, e);
        }
      }
    }
    if (!patterns.isEmpty()) {
      final SimplePackageFilter filter = new SimplePackageFilter(patterns);
      MetaClassFactory
        .getAllCachedClasses()
        .stream()
        .filter(mc -> filter.apply(mc.getFullyQualifiedName()))
        .collect(toCollection(() -> exposedClasses));
    }
  }

  private static void addSerializableTypes(final Set<MetaClass> exposedClasses, final String value) {
    final Set<String> patterns = new LinkedHashSet<>();
    for (final String s : value.split(" ")) {
      final String singleValue = s.trim();
      if (singleValue.isEmpty()){
        continue;
      }
      if (singleValue.endsWith("*")) {
        patterns.add(singleValue);
      }
      else {
        try {
          exposedClasses.add(MetaClassFactory.get(singleValue));
        }
        catch (final Exception e) {
          throw new RuntimeException("could not find class defined in ErraiApp.properties for serialization: "
                  + s, e);
        }
      }
    }
    if (!patterns.isEmpty()) {
      final SimplePackageFilter filter = new SimplePackageFilter(patterns);
      MetaClassFactory
        .getAllCachedClasses()
        .stream()
        .filter(mc -> filter.apply(mc.getFullyQualifiedName()) && validateWildcardSerializable(mc))
        .collect(toCollection(() -> exposedClasses));
    }
  }

  private static boolean validateWildcardSerializable(MetaClass mc) {
    if (mc.isInterface() || (mc.isAbstract() && !mc.isEnum())) {
      log.debug("Serializable types cannot be an interface or abstract, ignoring: {}", mc.getFullyQualifiedName());
      return false;
    }
    return true;
  }

  private static void updateFrameworkProperties(final Map<String, String> frameworkProps, final String key, final String value) {
    if (frameworkProps.containsKey(key)) {
      if (isListValuedProperty(key)) {
        // TODO should validate that different values don't conflict
        final String oldValue = frameworkProps.get(key);
        final String newValue = oldValue + " " + value;
        log.debug("Merging property {} = {}", key, newValue);
        frameworkProps.put(key, newValue);
      } else {
        log.warn("The property {} has been set multiple times.", key);
        frameworkProps.put(key, value);
      }
    } else {
      frameworkProps.put(key, value);
    }
  }

  private static boolean isListValuedProperty(final String key) {
    return key.equals(ErraiAppPropertiesErraiModulesConfiguration.IOC_ENABLED_ALTERNATIVES)
            || key.equals(ErraiAppPropertiesErraiModulesConfiguration.BINDABLE_TYPES)
            || key.equals(ErraiAppPropertiesErraiModulesConfiguration.SERIALIZABLE_TYPES)
            || key.equals(ErraiAppPropertiesErraiModulesConfiguration.NON_SERIALIZABLE_TYPES)
            || key.equals(CONFIG_ERRAI_MAPPING_ALIASES);
  }

  private static void addExposedInnerClasses(final Set<MetaClass> exposedClasses, final Set<MetaClass> exposedFromScanner) {
    for (final MetaClass cls : exposedFromScanner) {
      for (final MetaClass decl : cls.getDeclaredClasses()) {
        if (decl.isSynthetic()) {
          continue;
        }
        exposedClasses.add(decl);
      }
    }
  }

  public static Collection<URL> getErraiAppPropertiesFilesUrls() {
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    final ClassLoader envUtilClassLoader = EnvUtil.class.getClassLoader();

    return ErraiAppPropertiesFiles.getUrls(contextClassLoader, envUtilClassLoader);
  }

  private static void fillInInterfacesAndSuperTypes(final Set<MetaClass> set, final MetaClass type) {
    for (final MetaClass iface : type.getInterfaces()) {
      set.add(iface);
      fillInInterfacesAndSuperTypes(set, iface);
    }
    if (type.getSuperClass() != null) {
      fillInInterfacesAndSuperTypes(set, type.getSuperClass());
    }
  }

  public static void clearCache() {
    CacheUtil.getCache(EnvironmentConfigCache.class).clear();
  }

  /**
   * @return an instance of {@link EnvironmentConfig}. Do NOT retain a reference to this value. Call every time
   *         you need additional configuration information.
   */
  public static EnvironmentConfig getEnvironmentConfig() {
    return CacheUtil.getCache(EnvironmentConfigCache.class).get();
  }

  public static boolean isPortableType(final Class<?> cls) {
    final MetaClass mc = MetaClassFactory.get(cls);
    return isPortableType(mc);
  }

  public static boolean isPortableType(final MetaClass mc) {

    if (allSerializableTypes == null) {
       allSerializableTypes = new ErraiAppPropertiesConfiguration().modules().getSerializableTypes();
    }

    return mc.isAnnotationPresent(Portable.class)
            || allSerializableTypes.contains(mc)
            || String.class.getName().equals(mc.getFullyQualifiedName())
            || TypeHandlerFactory.getHandler(mc.unsafeAsClass()) != null;
  }

}
