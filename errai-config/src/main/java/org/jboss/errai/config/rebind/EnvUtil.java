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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.LocalEvent;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.common.rebind.CacheStore;
import org.jboss.errai.common.rebind.CacheUtil;
import org.jboss.errai.config.util.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {
  public static class EnvironmentConfigCache implements CacheStore {
    private volatile EnvironmentConfig environmentConfig;
    private final Map<String, String> permanentProperties = new ConcurrentHashMap<String, String>();

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

  public static final String CONFIG_ERRAI_SERIALIZABLE_TYPE = "errai.marshalling.serializableTypes";
  public static final String CONFIG_ERRAI_NONSERIALIZABLE_TYPE = "errai.marshalling.nonserializableTypes";
  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";
  public static final String CONFIG_ERRAI_IOC_ENABLED_ALTERNATIVES = "errai.ioc.enabled.alternatives";

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
    final Map<String, String> frameworkProps = new HashMap<String, String>();
    final Map<String, String> mappingAliases = new HashMap<String, String>();
    final Set<MetaClass> exposedClasses = new HashSet<MetaClass>();
    final Set<MetaClass> nonportableClasses = new HashSet<MetaClass>();
    final Set<String> explicitTypes = new HashSet<String>();
    final Set<MetaClass> portableNonExposed = new HashSet<MetaClass>();

    final Set<MetaClass> exposedFromScanner = new HashSet<MetaClass>(ClassScanner.getTypesAnnotatedWith(Portable.class));
    nonportableClasses.addAll(ClassScanner.getTypesAnnotatedWith(NonPortable.class));

    for (final MetaClass cls : exposedFromScanner) {
      for (final MetaClass decl : cls.getDeclaredClasses()) {
        if (decl.isSynthetic()) {
          continue;
        }
        exposedClasses.add(decl);
      }
    }
    exposedClasses.addAll(exposedFromScanner);

    final Collection<URL> erraiAppProperties = getErraiAppProperties();
    for (final URL url : erraiAppProperties) {
      InputStream inputStream = null;
      try {
        log.debug("checking " + url.getFile() + " for configured types ...");
        inputStream = url.openStream();

        final ResourceBundle props = new PropertyResourceBundle(inputStream);
        for (final Object o : props.keySet()) {
          final String key = (String) o;
          final String value = props.getString(key);
          if (frameworkProps.containsKey(key)) {
            if (key.equals(CONFIG_ERRAI_IOC_ENABLED_ALTERNATIVES)) {
              final String oldValue = frameworkProps.get(key);
              final String newValue = oldValue + " " + value;
              frameworkProps.put(key, newValue);
            } else {
              log.warn("The property {} has been set multiple times.", key);
              frameworkProps.put(key, value);
            }
          } else {
            frameworkProps.put(key, value);
          }

          for (final String s : value.split(" ")) {
            if ("".equals(s))
              continue;
            if (key.equals(CONFIG_ERRAI_SERIALIZABLE_TYPE)) {
              try {
                exposedClasses.add(MetaClassFactory.get(s.trim()));
                explicitTypes.add(s.trim());
              }
              catch (Exception e) {
                throw new RuntimeException("could not find class defined in ErraiApp.properties for serialization: "
                        + s, e);
              }
            }
            else if (key.equals(CONFIG_ERRAI_NONSERIALIZABLE_TYPE)) {
              try {
                nonportableClasses.add(MetaClassFactory.get(s.trim()));
              }
              catch (Exception e) {
                throw new RuntimeException("could not find class defined in ErraiApp.properties as nonserializable: "
                        + s, e);
              }
            }
            else if (key.equals(CONFIG_ERRAI_MAPPING_ALIASES)) {
              try {
                final String[] mapping = s.split("->");

                if (mapping.length != 2) {
                  throw new RuntimeException("syntax error: mapping for marshalling alias: " + s);
                }

                final Class<?> fromMapping = Class.forName(mapping[0].trim());
                final Class<?> toMapping = Class.forName(mapping[1].trim());

                mappingAliases.put(fromMapping.getName(), toMapping.getName());
                explicitTypes.add(fromMapping.getName());
                explicitTypes.add(toMapping.getName());
              }
              catch (Exception e) {
                throw new RuntimeException("could not find class defined in ErraiApp.properties for mapping: " + s, e);
              }
            }
          }
        }
      }
      catch (IOException e) {
        throw new RuntimeException("error reading ErraiApp.properties", e);
      }
      finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (IOException e) {
            //
          }
        }
      }
    }

    final Collection<MetaClass> exts = ClassScanner.getTypesAnnotatedWith(EnvironmentConfigExtension.class, true);
    for (final MetaClass cls : exts) {
      try {
        Class<? extends ExposedTypesProvider> providerClass = cls.asClass().asSubclass(ExposedTypesProvider.class);
        for (final MetaClass exposedType : providerClass.newInstance().provideTypesToExpose()) {
          if (exposedType.isPrimitive()) {
            exposedClasses.add(exposedType.asBoxed());
          }
          else if (exposedType.isConcrete()) {
            exposedClasses.add(exposedType);
          }
        }
      }
      catch (Throwable e) {
        throw new RuntimeException("unable to load environment extension: " + cls.getFullyQualifiedName(), e);
      }
    }

    // must do this before filling in interfaces and supertypes!
    exposedClasses.removeAll(nonportableClasses);

    for (final MetaClass cls : exposedClasses) {
      fillInInterfacesAndSuperTypes(portableNonExposed, cls);
    }

    return new EnvironmentConfig(mappingAliases, exposedClasses, portableNonExposed, explicitTypes, frameworkProps);
  }

  public static Collection<URL> getErraiAppProperties() {
    try {
      final Set<URL> urlList = new HashSet<URL>();
      for (ClassLoader classLoader : Arrays.asList(Thread.currentThread().getContextClassLoader(),
              EnvUtil.class.getClassLoader())) {

        Enumeration<URL> resources = classLoader.getResources("ErraiApp.properties");
        while (resources.hasMoreElements()) {
          urlList.add(resources.nextElement());
        }
      }
      return urlList;
    }
    catch (IOException e) {
      throw new RuntimeException("failed to load ErraiApp.properties from classloader", e);
    }
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
    return isUserPortableType(mc) || isString(mc) || isBuiltinPortableType(cls);
  }

  public static boolean isPortableType(final MetaClass mc) {
    return isUserPortableType(mc) || isString(mc) || isBuiltinPortableType(mc.asClass());
  }

  private static boolean isUserPortableType(final MetaClass mc) {
    return mc.isAnnotationPresent(Portable.class) || getEnvironmentConfig().getExposedClasses().contains(mc)
        || getEnvironmentConfig().getPortableSuperTypes().contains(mc);
  }

  private static boolean isString(final MetaClass mc) {
    return String.class.getName().equals(mc.getFullyQualifiedName());
  }

  private static boolean isBuiltinPortableType(final Class<?> cls) {
    return TypeHandlerFactory.getHandler(cls) != null;
  }

  public static boolean isLocalEventType(final Class<?> cls) {
    return cls.isAnnotationPresent(LocalEvent.class);
  }

  public static boolean isLocalEventType(final MetaClass cls) {
    return cls.isAnnotationPresent(LocalEvent.class);
  }

  public static Set<Class<?>> getAllPortableConcreteSubtypes(final Class<?> clazz) {
    final Set<Class<?>> portableSubtypes = new HashSet<Class<?>>();
    if (isPortableType(clazz)) {
      portableSubtypes.add(clazz);
    }

    for (final Class<?> subType : ScannerSingleton.getOrCreateInstance().getSubTypesOf(clazz)) {
      if (isPortableType(subType)) {
        portableSubtypes.add(subType);
      }
    }

    return portableSubtypes;
  }

  public static Set<Class<?>> getAllPortableSubtypes(final Class<?> clazz) {
    final Set<Class<?>> portableSubtypes = new HashSet<Class<?>>();
    if (clazz.isInterface() || isPortableType(clazz)) {
      portableSubtypes.add(clazz);
    }

    for (final Class<?> subType : ScannerSingleton.getOrCreateInstance().getSubTypesOf(clazz)) {
      if (clazz.isInterface() || isPortableType(subType)) {
        portableSubtypes.add(subType);
      }
    }

    return portableSubtypes;
  }

}
