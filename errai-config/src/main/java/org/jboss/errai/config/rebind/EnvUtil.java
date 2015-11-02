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
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Arrays;
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

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.QuickDeps;
import org.jboss.errai.common.client.api.annotations.LocalEvent;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.common.rebind.CacheStore;
import org.jboss.errai.common.rebind.CacheUtil;
import org.jboss.errai.config.util.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.ext.GeneratorContext;

/**
 * @author Mike Brock
 */
public abstract class EnvUtil {
  public static class EnvironmentConfigCache implements CacheStore {
    private volatile EnvironmentConfig environmentConfig;

    public EnvironmentConfigCache() {
      clear();
    }

    @Override
    public synchronized void clear() {
      environmentConfig = newEnvironmentConfig();
    }

    public synchronized EnvironmentConfig get() {
      return environmentConfig;
    }
  }

  public static final String CONFIG_ERRAI_SERIALIZABLE_TYPE = "errai.marshalling.serializableTypes";
  public static final String CONFIG_ERRAI_NONSERIALIZABLE_TYPE = "errai.marshalling.nonserializableTypes";
  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";
  public static final String CONFIG_ERRAI_IOC_ENABLED_ALTERNATIVES = "errai.ioc.enabled.alternatives";
  public static final String SYSPROP_USE_REACHABILITY_ANALYSIS = "errai.compile.perf.perform_reachability_analysis";

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

  public static boolean isLocalEventType(final Class<?> cls) {
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

  static class ReachabilityCache {
    private volatile GeneratorContext _lastContext;
    private final Map<String, ReachableTypes> moduleToReachableTypes = new ConcurrentHashMap<String, ReachableTypes>();

    public boolean isCacheValid(final GeneratorContext context) {
      return isCacheValid(context, RebindUtils.getModuleName(context));

    }

    public boolean isCacheValid(final GeneratorContext context, final String moduleName) {
      return _lastContext == context && moduleToReachableTypes.containsKey(moduleName);
    }

    public ReachableTypes getCache(final GeneratorContext context) {
      final String moduleName = RebindUtils.getModuleName(context);
      if (isCacheValid(context, moduleName)) {
        return moduleToReachableTypes.get(moduleName);
      }
      else {
        return null;
      }
    }

    public void putCache(final GeneratorContext context, final ReachableTypes reachableTypes) {
      if (_lastContext == null) {
        _lastContext = context;
      }
      else if (_lastContext != context) {
        _lastContext = context;
        moduleToReachableTypes.clear();
      }

      moduleToReachableTypes.put(RebindUtils.getModuleName(context), reachableTypes);
    }
  }


  private static volatile SoftReference<ReachabilityCache> reachabilityCache = null;

  private static final Set<String> reachabilityExclusionNegative = new HashSet<String>();
  private static final Set<String> reachabilityClassExclusionList = new HashSet<String>() {
    {
      add("org.jboss.errai.bus.client.framework.ClientMessageBusImpl");
    }
  };

  private static final Set<String> reachabilityPackageExclusionList = new HashSet<String>() {
    {
      /**
       * These packages are generally excluded to improve devmode/testing performance. This explicit
       * exclusion doesn't really have an effect during production compiles.
       */
      add("com.google.gwt");
      add("java");
      add("javax");
    }
  };

  private static boolean isReachabilityExcluded(final String packageName) {
    if (packageName == null) return false;

    if (reachabilityExclusionNegative.contains(packageName)) {
      return false;
    }
    else if (reachabilityPackageExclusionList.contains(packageName)) {
      return true;
    }

    boolean found = false;
    for (final String pkg : reachabilityPackageExclusionList) {
      if (packageName.startsWith(pkg)) {
        found = true;
      }
    }

    if (found) {
      reachabilityPackageExclusionList.add(packageName);
      return true;
    }
    else {
      reachabilityExclusionNegative.add(packageName);
      return false;
    }

  }

  public static ReachableTypes getAllReachableClasses(final GeneratorContext context) {
    if (System.getProperty(SYSPROP_USE_REACHABILITY_ANALYSIS) == null
        || !Boolean.getBoolean(SYSPROP_USE_REACHABILITY_ANALYSIS)) {

      log.warn("reachability analysis disabled. errai may generate unnecessary code.");
      log.warn("enable reachability analysis with -D" + SYSPROP_USE_REACHABILITY_ANALYSIS + "=true");
      return ReachableTypes.EVERYTHING_REACHABLE_INSTANCE;
    }

    ReachabilityCache cache;
    if (reachabilityCache == null || (cache = reachabilityCache.get()) == null) {
      reachabilityCache = new SoftReference<ReachabilityCache>(cache = new ReachabilityCache());
    }

    if (cache.isCacheValid(context)) {
      return cache.getCache(context);
    }


    EnvUtil.clearCache();
    final EnvironmentConfig config = getEnvironmentConfig();

    long time = System.currentTimeMillis();

    final Set<String> packages = new HashSet<String>();

    if (isJUnitTest()) {
      packages.addAll(RebindUtils.findTranslatablePackagesInModule(context));
    }
    else {
      packages.addAll(RebindUtils.getOuterTranslatablePackages(context));
    }

    class Reachability {
      private final Set<String> packages;
      private final Set<String> negativeHits = new HashSet<String>();

      Reachability(final Set<String> packages) {
        this.packages = new HashSet<String>(packages);
      }

      public boolean isReachablePackage(final String pkg) {
        if (pkg == null || packages.contains(pkg)) {
          return true;
        }
        if (negativeHits.contains(pkg)) {
          return false;
        }

        for (final String p : packages) {
          if (pkg.startsWith(p)) {
            packages.add(pkg);
            return true;
          }
        }

        negativeHits.add(pkg);
        return false;
      }
    }

    final Set<String> allDependencies = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(100));
    final Collection<MetaClass> allCachedClasses = MetaClassFactory.getAllCachedClasses();
    final ClassLoader classLoader = EnvUtil.class.getClassLoader();

    final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    final Reachability reachability = new Reachability(packages);

    try {
      for (final MetaClass mc : allCachedClasses) {
        String fullyQualifiedName = mc.getFullyQualifiedName();
        int splitPoint;
        while ((splitPoint = fullyQualifiedName.lastIndexOf('$')) != -1) {
          fullyQualifiedName = fullyQualifiedName.substring(0, splitPoint);
        }

        if (mc.isPrimitive() || mc.isArray()) {
          continue;
        }
        else if (isReachabilityExcluded(mc.getPackageName())) {
          continue;
        }
        else if (!config.getExplicitTypes().contains(fullyQualifiedName)
            && !reachability.isReachablePackage(mc.getPackageName())) {
          continue;
        }

        final URL resource = classLoader.getResource(fullyQualifiedName.replace('.', '/') + ".java");

        if (resource != null) {
          InputStream stream = null;
          try {
            stream = new BufferedInputStream(resource.openStream());
            final byte[] readBuffer = new byte[stream.available()];
            stream.read(readBuffer);

            if (log.isDebugEnabled()) {
              log.debug("scanning " + fullyQualifiedName + " for reachable types ...");
            }
            executor.execute(new ReachabilityRunnable(readBuffer, allDependencies));
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
      log.warn("the reachability analysis was interrupted", e);
      cache.putCache(context, ReachableTypes.EVERYTHING_REACHABLE_INSTANCE);
      return ReachableTypes.EVERYTHING_REACHABLE_INSTANCE;
    }

    if (log.isDebugEnabled()) {
      log.debug("*** REACHABILITY ANALYSIS (production mode: " + EnvUtil.isProdMode() + ") ***");
      for (final String s : allDependencies) {
        log.debug(" -> " + s);
      }

      time = System.currentTimeMillis() - time;

      log.debug("*** END OF REACHABILITY ANALYSIS (" + time + "ms) *** ");
    }

    final ReachableTypes reachableTypes = new ReachableTypes(allDependencies, true);
    cache.putCache(context, reachableTypes);
    return reachableTypes;
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
