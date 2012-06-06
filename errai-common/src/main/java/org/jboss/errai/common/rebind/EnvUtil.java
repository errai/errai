/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.common.rebind;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {
  public static final String CONFIG_ERRAI_SERIALIZABLE_TYPE = "errai.marshalling.serializableTypes";
  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";

  private static volatile Boolean _isJUnitTest;

  public static boolean isJUnitTest() {
    if (_isJUnitTest != null) return _isJUnitTest;

    for (StackTraceElement el : new Throwable().getStackTrace()) {
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

    for (StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.dev.shell.OophmSessionHandler")) {
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

  private static EnviromentConfig loadConfiguredPortableTypes() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    final Map<String, String> mappingAliases = new HashMap<String, String>();
    final Set<Class<?>> exposedClasses = new HashSet<Class<?>>();
    final Set<Class<?>> portableNonExposed = new HashSet<Class<?>>();

    final Set<Class<?>> exposedFromScanner = new HashSet<Class<?>>(scanner.getTypesAnnotatedWith(Portable.class));

    for (Class<?> cls : exposedFromScanner) {
      for (Class<?> decl : cls.getDeclaredClasses()) {
        if (decl.isSynthetic()) {
          continue;
        }

        exposedClasses.add(decl);
      }
    }

    exposedClasses.addAll(exposedFromScanner);

    final Enumeration<URL> erraiAppProperties;

    try {
      erraiAppProperties = Thread.currentThread().getContextClassLoader()
              .getResources("ErraiApp.properties");
    }
    catch (IOException e) {
      throw new RuntimeException("failed to load ErraiApp.properties from classloader", e);
    }


    //  new PropertyResourceBundle(erraiAppProperties.nextElement().openStream())

    while (erraiAppProperties.hasMoreElements()) {
      InputStream inputStream = null;
      try {
        final URL url = erraiAppProperties.nextElement();

        log.debug("checking " + url.getFile() + " for configured types ...");

        inputStream = url.openStream();

        ResourceBundle props = new PropertyResourceBundle(inputStream);
        if (props != null) {

          for (Object o : props.keySet()) {
            String key = (String) o;
            if (key.equals(CONFIG_ERRAI_SERIALIZABLE_TYPE)) {
              for (String s : props.getString(key).split(" ")) {
                try {
                  Class<?> cls = Class.forName(s.trim());
                  exposedClasses.add(cls);
                }
                catch (Exception e) {
                  throw new RuntimeException("could not find class defined in ErraiApp.properties for serialization: " + s);
                }
              }

              break;
            }

            if (key.equals(CONFIG_ERRAI_MAPPING_ALIASES)) {
              for (String s : props.getString(key).split(" ")) {
                try {
                  String[] mapping = s.split("->");

                  if (mapping.length != 2) {
                    throw new RuntimeException("syntax error: mapping for marshalling alias: " + s);
                  }

                  Class<?> fromMapping = Class.forName(mapping[0].trim());
                  Class<?> toMapping = Class.forName(mapping[1].trim());

                  mappingAliases.put(fromMapping.getName(), toMapping.getName());
                }
                catch (Exception e) {
                  throw new RuntimeException("could not find class defined in ErraiApp.properties for mapping: " + s);
                }
              }
              break;
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

    for (Class<?> cls : exposedClasses) {
      fillInInterfacesAndSuperTypes(portableNonExposed, cls);
    }

    return new EnviromentConfig(mappingAliases, exposedClasses, portableNonExposed);
  }

  private static void fillInInterfacesAndSuperTypes(Set<Class<?>> set, Class<?> type) {
    for (Class<?> iface : type.getInterfaces()) {
      set.add(iface);
      fillInInterfacesAndSuperTypes(set, iface);
    }
    if (type.getSuperclass() != null) {
      fillInInterfacesAndSuperTypes(set, type.getSuperclass());
    }
  }

  private static EnviromentConfig _environmentConfigCache;

  public static EnviromentConfig getEnvironmentConfig() {
    if (_environmentConfigCache == null) _environmentConfigCache = loadConfiguredPortableTypes();
    return _environmentConfigCache;
  }

  public static boolean isPortableType(Class<?> cls) {
    if (cls.isAnnotationPresent(Portable.class) || getEnvironmentConfig().getExposedClasses().contains(cls)
            || getEnvironmentConfig().getPortableSuperTypes().contains(cls)) {
      return true;
    }
    else {
      if (String.class.equals(cls) || TypeHandlerFactory.getHandler(cls) != null) {
        return true;
      }
    }
    return false;
  }

  public static Set<Class<?>> getAllPortableConcreteSubtypes(final Class<?> clazz) {
    final Set<Class<?>> portableSubtypes = new HashSet<Class<?>>();
    if (isPortableType(clazz)) {
      portableSubtypes.add(clazz);
    }

    for (Class<?> subType : ScannerSingleton.getOrCreateInstance().getSubTypesOf(clazz)) {
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

    for (Class<?> subType : ScannerSingleton.getOrCreateInstance().getSubTypesOf(clazz)) {
      if (clazz.isInterface() || isPortableType(subType)) {
        portableSubtypes.add(subType);
      }
    }

    return portableSubtypes;
  }
}
