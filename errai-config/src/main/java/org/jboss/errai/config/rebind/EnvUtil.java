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

package org.jboss.errai.config.rebind;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gwt.thirdparty.guava.common.io.Files;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.QuickDeps;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.config.util.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.ext.GeneratorContext;

import javax.xml.transform.stream.StreamSource;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {
  public static final String CONFIG_ERRAI_SERIALIZABLE_TYPE = "errai.marshalling.serializableTypes";
  public static final String CONFIG_ERRAI_NONSERIALIZABLE_TYPE = "errai.marshalling.nonserializableTypes";
  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";

  public static final String SYSPROP_USE_REACHABILITY_ANALYSIS = "errai.compile.perf.perform_reachability_analysis";

  private static final Logger logger = LoggerFactory.getLogger("Env");

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

    final Enumeration<URL> erraiAppProperties;

    try {
      erraiAppProperties = Thread.currentThread().getContextClassLoader()
          .getResources("ErraiApp.properties");
    }
    catch (IOException e) {
      throw new RuntimeException("failed to load ErraiApp.properties from classloader", e);
    }

    while (erraiAppProperties.hasMoreElements()) {
      InputStream inputStream = null;
      try {
        final URL url = erraiAppProperties.nextElement();

        log.debug("checking " + url.getFile() + " for configured types ...");

        inputStream = url.openStream();

        final ResourceBundle props = new PropertyResourceBundle(inputStream);
        if (props != null) {

          for (final Object o : props.keySet()) {
            final String key = (String) o;

            frameworkProps.put(key, props.getString(key));

            if (key.equals(CONFIG_ERRAI_SERIALIZABLE_TYPE)) {
              for (final String s : props.getString(key).split(" ")) {
                try {
                  exposedClasses.add(MetaClassFactory.get(s.trim()));
                  explicitTypes.add(s.trim());
                }
                catch (Exception e) {
                  throw new RuntimeException("could not find class defined in ErraiApp.properties for serialization: " + s);
                }
              }

              continue;
            }

            if (key.equals(CONFIG_ERRAI_NONSERIALIZABLE_TYPE)) {
              for (final String s : props.getString(key).split(" ")) {
                try {
                  nonportableClasses.add(MetaClassFactory.get(s.trim()));
                }
                catch (Exception e) {
                  throw new RuntimeException("could not find class defined in ErraiApp.properties as nonserializable: " + s);
                }
              }

              continue;
            }

            if (key.equals(CONFIG_ERRAI_MAPPING_ALIASES)) {
              for (final String s : props.getString(key).split(" ")) {
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
                  throw new RuntimeException("could not find class defined in ErraiApp.properties for mapping: " + s);
                }
              }
              continue;
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

    // must do this before filling in interfaces and supertypes!
    exposedClasses.removeAll(nonportableClasses);

    for (final MetaClass cls : exposedClasses) {
      fillInInterfacesAndSuperTypes(portableNonExposed, cls);
    }

    return new EnviromentConfig(mappingAliases, exposedClasses, portableNonExposed, explicitTypes, frameworkProps);
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

  private static EnviromentConfig _environmentConfigCache;

  public static EnviromentConfig getEnvironmentConfig() {
    if (_environmentConfigCache == null) _environmentConfigCache = loadConfiguredPortableTypes();
    return _environmentConfigCache;
  }

  public static boolean isPortableType(final Class<?> cls) {
    final MetaClass mc = MetaClassFactory.get(cls);
    if (cls.isAnnotationPresent(Portable.class) || getEnvironmentConfig().getExposedClasses().contains(mc)
        || getEnvironmentConfig().getPortableSuperTypes().contains(mc)) {
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

  public static void clearCaches() {
    _environmentConfigCache = null;
  }

  private static volatile GeneratorContext _lastContext;
  private static volatile ReachableTypes _reachableCache;

  public static ReachableTypes getAllReachableClasses(final GeneratorContext context) {
    if (_lastContext == context && _reachableCache != null) {
      return _reachableCache;
    }

    final EnviromentConfig config = getEnvironmentConfig();

    if (System.getProperty(SYSPROP_USE_REACHABILITY_ANALYSIS) != null
        && !Boolean.getBoolean(SYSPROP_USE_REACHABILITY_ANALYSIS)) {

      log.warn("reachability analysis disabled. errai may generate unnecessary code.");
      log.warn("enable reachability analysis with -D" + SYSPROP_USE_REACHABILITY_ANALYSIS + "=true");
      return ReachableTypes.EVERYTHING_REACHABLE_INSTANCE;
    }

    long time = System.currentTimeMillis();

    final Set<String> packages = RebindUtils.findTranslatablePackagesInModule(context);

    final Set<String> allDeps = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(100));
    final Collection<MetaClass> allCachedClasses = MetaClassFactory.getAllCachedClasses();
    final ClassLoader classLoader = EnvUtil.class.getClassLoader();

    final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    try {
      for (final MetaClass mc : allCachedClasses) {
        if (mc.isInterface()) continue;

        String fullyQualifiedName = mc.getFullyQualifiedName();
        int splitPoint;
        while ((splitPoint = fullyQualifiedName.lastIndexOf('$')) != -1) {
          fullyQualifiedName = fullyQualifiedName.substring(0, splitPoint);
        }

        if (!config.getExplicitTypes().contains(fullyQualifiedName)
            && !packages.contains(mc.getPackageName())) continue;

        final URL resource = classLoader.getResource(fullyQualifiedName.replaceAll("\\.", "/") + ".java");

        if (resource != null) {
          InputStream stream = null;
          try {
            stream = new BufferedInputStream(resource.openStream());
            final byte[] readBuffer = new byte[stream.available()];
            stream.read(readBuffer);

            executor.execute(new ReachabilityRunnable(readBuffer, allDeps));
          }
          catch (IOException e) {
            log.warn("could not open resource: " + resource.getFile());
          }
          finally {
            if (stream != null) {
              stream.close();
            }
          }
        }
        else {
          log.warn("source for " + fullyQualifiedName + " is missing.");
        }
      }
    }
    catch (Throwable e) {
      e.printStackTrace();
    }

    try {
      executor.shutdown();
      executor.awaitTermination(60, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (log.isDebugEnabled()) {
      log.debug("*** REACHABILITY ANALYSIS (production mode: " + EnvUtil.isProdMode() + ") ***");
      for (final String s : allDeps) {
        log.debug(" -> " + s);
      }

      time = System.currentTimeMillis() - time;

      log.debug("*** END OF REACHABILITY ANALYSIS (" + time + "ms) *** ");
    }

    _lastContext = context;
    return _reachableCache = new ReachableTypes(allDeps, true);
  }

  private static class ReachabilityRunnable implements Runnable {
    private final byte[] sourceBuffer;
    private final Set<String> results;

    private ReachabilityRunnable(final byte[] sourceBuffer, final Set<String> results) {
      this.sourceBuffer = sourceBuffer;
      this.results = results;
    }

    @Override
    public void run() {
      results.addAll(QuickDeps.getQuickTypeDependencyList(new String(sourceBuffer), null));
    }
  }
}
