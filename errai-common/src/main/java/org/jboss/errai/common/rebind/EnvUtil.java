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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {
  public static final String CONFIG_ERRAI_SERIALIZABLE_TYPE = "errai.marshalling.serializableTypes";
  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";


  public static boolean isJUnitTest() {
    for (StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.junit.client.")
              || el.getClassName().startsWith("org.junit")) {
        return true;
      }
    }
    return false;
  }

  public static boolean isDevMode() {
    for (StackTraceElement el : new Throwable().getStackTrace()) {
      if (el.getClassName().startsWith("com.google.gwt.dev.shell.OophmSessionHandler")) {
        return true;
      }
    }
    return false;
  }

  private static Logger log = LoggerFactory.getLogger(EnvUtil.class);

  private static EnviromentConfig loadConfiguredPortableTypes() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    final Map<String, String> mappingAliases = new HashMap<String, String>();
    final Set<Class<?>> exposedClasses = new HashSet<Class<?>>();

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

    Properties props = scanner.getProperties("ErraiApp.properties");
    if (props != null) {
      log.debug("checking ErraiApp.properties for configured types ...");

      for (Object o : props.keySet()) {
        String key = (String) o;
        if (key.equals(CONFIG_ERRAI_SERIALIZABLE_TYPE)) {
          for (String s : props.getProperty(key).split(" ")) {
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
          for (String s : props.getProperty(key).split(" ")) {
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
    return new EnviromentConfig(mappingAliases, exposedClasses);
  }

  private static EnviromentConfig _environmentConfigCache;

  public static EnviromentConfig getEnvironmentConfig() {
    if (_environmentConfigCache == null) _environmentConfigCache = loadConfiguredPortableTypes();
    return _environmentConfigCache;
  }

  public static boolean isPortableType(Class cls) {
    if (cls.isAnnotationPresent(Portable.class) || getEnvironmentConfig().getExposedClasses().contains(cls)) {
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
