/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.metadata;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.jboss.errai.common.rebind.CacheStore;
import org.jboss.errai.common.rebind.CacheUtil;
import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.ReflectionsException;
import org.jboss.errai.reflections.scanners.FieldAnnotationsScanner;
import org.jboss.errai.reflections.scanners.MethodAnnotationsScanner;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * Scans component meta data. The scanner creates a {@link DeploymentContext} that identifies nested
 * subdeployments (i.e. WAR inside EAR) and processes the resulting archive Url's using the <a
 * href="http://code.google.com/p/reflections/">Reflections</a> library.
 * <p/>
 * <p/>
 * The initial set of config URLs (entry points) is discovered through ErraiApp.properties.
 * 
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class MetaDataScanner extends Reflections {
  private static final Logger log = LoggerFactory.getLogger(MetaDataScanner.class);
  private static final String EXTENSION_KEY = "errai.class_scanning_extension";

  public static class CacheHolder implements CacheStore {
    final Map<String, Set<SortableClassFileWrapper>> ANNOTATIONS_TO_CLASS = new ConcurrentHashMap<String, Set<SortableClassFileWrapper>>();

    @Override
    public void clear() {
      ANNOTATIONS_TO_CLASS.clear();
    }
  }

  public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";

  private static final ErraiPropertyScanner propScanner = new ErraiPropertyScanner(new Predicate<String>() {
    @Override
    public boolean apply(final String file) {
      return file.endsWith(".properties");
    }
  });

  MetaDataScanner(final List<URL> urls, File cacheFile) {
    super(getConfiguration(urls));
    try {
      for (final Class<? extends Vfs.UrlType> cls : findExtensions()) {
        try {
          final Vfs.UrlType urlType = cls.asSubclass(Vfs.UrlType.class).newInstance();
          registerTypeHandler(urlType);
          log.info("added class scanning extensions: " + cls.getName());
        } catch (Throwable t) {
          throw new RuntimeException("could not load scanner extension: " + cls.getName(), t);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (cacheFile != null) {
      collect(cacheFile);
    }
    else {
      scan();
    }
  }

  private List<Class<? extends Vfs.UrlType>> findExtensions() {
    final Collection<URL> erraiAppProperties = getErraiAppProperties();

    final List<Class<? extends Vfs.UrlType>> extensions = new ArrayList<Class<? extends Vfs.UrlType>>();

    for (URL url : erraiAppProperties) {
      InputStream inputStream = null;
      try {
        inputStream = url.openStream();

        final ResourceBundle props = new PropertyResourceBundle(inputStream);
        if (props != null) {

          for (final Object o : props.keySet()) {
            final String key = (String) o;

            if (key.equals(EXTENSION_KEY)) {
              final String clsName = props.getString(key);
              try {

                final Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(clsName);
                extensions.add(aClass.asSubclass(Vfs.UrlType.class));
              } catch (Throwable t) {
                throw new RuntimeException("could not load class scanning extension: " + clsName, t);
              }
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("error reading ErraiApp.properties", e);
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            //
          }
        }
      }
    }
    return extensions;
  }

  private static Collection<URL> getErraiAppProperties() {
    try {
      final Set<URL> urlList = new HashSet<URL>();
      Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("ErraiApp.properties");

      while (resources.hasMoreElements()) {
        urlList.add(resources.nextElement());
      }

      resources = MetaDataScanner.class.getClassLoader().getResources("ErraiApp.properties");
      while (resources.hasMoreElements()) {
        urlList.add(resources.nextElement());
      }

      return urlList;
    } catch (IOException e) {
      throw new RuntimeException("failed to load ErraiApp.properties from classloader", e);
    }
  }

  private static Configuration getConfiguration(final List<URL> urls) {
    return new ConfigurationBuilder()
            .setUrls(urls)
            .setExecutorService(Executors.newFixedThreadPool(2))
            .setScanners(new FieldAnnotationsScanner(), new MethodAnnotationsScanner(),
                    new ExtendedTypeAnnotationScanner(), propScanner);
  }

  static MetaDataScanner createInstanceFromCache() {
    try {
      return createInstance(getConfigUrls(), RebindUtils.getCacheFile(RebindUtils.getClasspathHash() + ".cache.xml"));
    } catch (ReflectionsException e) {
      e.printStackTrace();
      return createInstance();
    }
  }

  static MetaDataScanner createInstance() {
    return createInstance(getConfigUrls(), null);
  }

  public static MetaDataScanner createInstance(final List<URL> urls) {
    return createInstance(urls, null);
  }

  public static MetaDataScanner createInstance(final List<URL> urls, final File cacheFile) {
    registerDefaultHandlers();

    final DeploymentContext ctx = new DeploymentContext(urls);
    final List<URL> actualUrls = ctx.process();
    final MetaDataScanner scanner = new MetaDataScanner(actualUrls, cacheFile);
    ctx.close(); // needs to closed after the scanner was created

    return scanner;
  }

  public static void registerTypeHandler(Vfs.UrlType handler) {
    Vfs.addDefaultURLTypes(handler);
  }

  private static void registerDefaultHandlers() {
    final List<Vfs.UrlType> urlTypes = Vfs.getDefaultUrlTypes();
    urlTypes.add(new WarUrlType());
    urlTypes.add(new LeafUrlType(".jnilib"));
    urlTypes.add(new LeafUrlType(".zip"));
    urlTypes.add(new LeafUrlType(".pom"));
    // thread safe?
    Vfs.setDefaultURLTypes(urlTypes);
  }

  public Set<Class<?>> getTypesAnnotatedWithExcluding(final Class<? extends Annotation> annotation,
          final String excludeRegex) {
    final Pattern p = Pattern.compile(excludeRegex);

    final Set<String> result = new HashSet<String>();
    final Set<String> types = getStore().getTypesAnnotatedWith(annotation.getName());
    for (final String className : types) {
      if (!p.matcher(className).matches())
        result.add(className);
    }

    return ImmutableSet.copyOf(forNames(result));
  }

  public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation,
          final Collection<String> packages) {
    final Set<Class<?>> results = new HashSet<Class<?>>();
    for (final Class<?> cls : getTypesAnnotatedWith(annotation)) {
      if (packages.contains(cls.getPackage().getName())) {
        results.add(cls);
      }
    }
    return results;
  }

  public Set<Method> getMethodsAnnotatedWithExcluding(final Class<? extends Annotation> annotation,
          final String excludeRegex) {
    final Set<Method> results = new HashSet<Method>();
    final Pattern p = Pattern.compile(excludeRegex);
    for (final Method method : getMethodsAnnotatedWith(annotation)) {
      if (!p.matcher(method.getClass().getName()).matches()) {
        results.add(method);
      }
    }
    return results;
  }

  public Set<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation,
          final Collection<String> packages) {
    final Set<Method> results = new HashSet<Method>();
    for (final Method method : getMethodsAnnotatedWith(annotation)) {
      if (packages.contains(method.getDeclaringClass().getPackage().getName())) {
        results.add(method);
      }
    }
    return results;
  }

  public Set<Field> getFieldsAnnotatedWith(final Class<? extends Annotation> annotation,
          final Collection<String> packages) {
    final Set<Field> results = new HashSet<Field>();
    for (final Field field : getFieldsAnnotatedWith(annotation)) {
      if (packages.contains(field.getDeclaringClass().getPackage().getName())) {
        results.add(field);
      }
    }
    return results;
  }

  private final Map<Class<? extends Annotation>, Set<Class<?>>> _annotationCache = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();

  @Override
  public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
    Set<Class<?>> types = _annotationCache.get(annotation);
    if (types == null) {
      types = new HashSet<Class<?>>(super.getTypesAnnotatedWith(annotation));

      if (annotation.isAnnotationPresent(Inherited.class)) {
        for (final Class<?> cls : new ArrayList<Class<?>>(types)) {
          types.addAll(getSubTypesOf(cls));
        }
      }

      _annotationCache.put(annotation, types);
    }

    return types;
  }

  public String getHashForTypesAnnotatedWith(final String seed, final Class<? extends Annotation> annotation) {
    if (!CacheUtil.getCache(CacheHolder.class).ANNOTATIONS_TO_CLASS.containsKey(annotation.getName())) {
      return "0";
    }
    else {
      try {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");

        if (seed != null) {
          md.update(seed.getBytes());
        }

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (final SortableClassFileWrapper classFileWrapper : CacheUtil.getCache(CacheHolder.class).ANNOTATIONS_TO_CLASS
                .get(annotation.getName())) {
          byteArrayOutputStream.reset();
          final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
          classFileWrapper.getClassFile().write(dataOutputStream);
          dataOutputStream.flush();
          md.update(byteArrayOutputStream.toByteArray());
        }

        return RebindUtils.hashToHexString(md.digest());

      } catch (Exception e) {
        throw new RuntimeException("could not generate hash", e);
      }
    }
  }

  public static List<URL> getConfigUrls(final ClassLoader loader) {
    try {
      final Enumeration<URL> configTargets = loader.getResources(ERRAI_CONFIG_STUB_NAME);
      final List<URL> urls = new ArrayList<URL>();

      while (configTargets.hasMoreElements()) {
        final URL url = configTargets.nextElement();

        try {
          final Properties properties = new Properties();
          final InputStream stream = url.openStream();
          try {
            properties.load(stream);
          } finally {
            stream.close();
          }
        } catch (IOException e) {
          System.err.println("could not read properties file");
          e.printStackTrace();
        }

        String urlString = url.toExternalForm();
        urlString = urlString.substring(0, urlString.indexOf(ERRAI_CONFIG_STUB_NAME));
        // URLs returned by the classloader are UTF-8 encoded. The URLDecoder assumes
        // a HTML form encoded String, which is why we escape the plus symbols here.
        // Otherwise, they would be decoded into space characters.
        // The pound character still must not appear anywhere in the path!
        urls.add(new URL(URLDecoder.decode(urlString.replaceAll("\\+", "%2b"), "UTF-8")));
      }
      return urls;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to scan configuration Url's", e);
    }
  }

  public static List<URL> getConfigUrls() {
    return getConfigUrls(MetaDataScanner.class.getClassLoader());
  }

  public Multimap<String, String> getErraiProperties() {
    return propScanner.getProperties();
  }
}
