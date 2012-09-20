/*
 * Copyright 2009 JBoss, a division Red Hat, Inc
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import com.google.common.collect.Multimap;
import javassist.bytecode.ClassFile;

import org.jboss.errai.common.client.framework.ErraiAppAttribs;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.jboss.errai.reflections.Configuration;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.ReflectionsException;
import org.jboss.errai.reflections.scanners.FieldAnnotationsScanner;
import org.jboss.errai.reflections.scanners.MethodAnnotationsScanner;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.vfs.Vfs;

/**
 * Scans component meta data. The scanner creates a {@link DeploymentContext} that identifies nested subdeployments
 * (i.e. WAR inside EAR) and processes the resulting archive Url's using the <a
 * href="http://code.google.com/p/reflections/">Reflections</a> library.
 * <p/>
 * <p/>
 * The initial set of config URLs (entry points) is discovered through ErraiApp.properties.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MetaDataScanner extends Reflections {
  public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";


  private static final ErraiPropertyScanner propScanner = new ErraiPropertyScanner(
      new Predicate<String>() {
        public boolean apply(final String file) {
          return file.endsWith(".properties");
        }
      }
  );

  MetaDataScanner(final List<URL> urls, File cacheFile) {
    super(getConfiguration(urls));

    if (cacheFile != null) {
      collect(cacheFile);
    }
    else {
      scan();
    }
  }

  static final Map<String, Set<SortableClassFileWrapper>> annotationsToClassFile =
      new ConcurrentHashMap<String, Set<SortableClassFileWrapper>>();

  static class SortableClassFileWrapper implements Comparable<SortableClassFileWrapper> {
    private String name;
    private ClassFile classFile;

    SortableClassFileWrapper(final String name, final ClassFile classFile) {
      this.name = name;
      this.classFile = classFile;
    }

    public ClassFile getClassFile() {
      return classFile;
    }

    @Override
    public int compareTo(final SortableClassFileWrapper o) {
      return name.compareTo(o.name);
    }
  }

  private static Configuration getConfiguration(final List<URL> urls) {
    return new ConfigurationBuilder()
        .setUrls(urls)
        .setExecutorService(Executors.newFixedThreadPool(2))
        .setScanners(
            new FieldAnnotationsScanner(),
            new MethodAnnotationsScanner(),
            new ExtendedTypeAnnotationScanner(),
            propScanner
        );
  }

  static MetaDataScanner createInstanceFromCache() {
    try {
      return createInstance(getConfigUrls(), RebindUtils.getCacheFile(RebindUtils.getClasspathHash() + ".cache.xml"));
    }
    catch (ReflectionsException e) {
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
    registerUrlTypeHandlers();

    final DeploymentContext ctx = new DeploymentContext(urls);
    final List<URL> actualUrls = ctx.process();
    final MetaDataScanner scanner = new MetaDataScanner(actualUrls, cacheFile);
    ctx.close(); // needs to closed after the scanner was created

    return scanner;
  }

  private static void registerUrlTypeHandlers() {
    final List<Vfs.UrlType> urlTypes = Vfs.getDefaultUrlTypes();
    urlTypes.add(new VfsUrlType());
    urlTypes.add(new WarUrlType());

    // thread safe?
    Vfs.setDefaultURLTypes(urlTypes);
  }

  public Set<Class<?>> getTypesAnnotatedWithExcluding(
      final Class<? extends Annotation> annotation, final String excludeRegex) {
    final Pattern p = Pattern.compile(excludeRegex);

    final Set<String> result = new HashSet<String>();
    final Set<String> types = getStore().getTypesAnnotatedWith(annotation.getName());
    for (final String className : types) {
      if (!p.matcher(className).matches())
        result.add(className);
    }

    return ImmutableSet.copyOf(forNames(result));
  }

  public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation, final Collection<String> packages) {
    final Set<Class<?>> results = new HashSet<Class<?>>();
    for (final Class<?> cls : getTypesAnnotatedWith(annotation)) {
      if (packages.contains(cls.getPackage().getName())) {
        results.add(cls);
      }
    }
    return results;
  }

  public Set<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation, final Collection<String> packages) {
    final Set<Method> results = new HashSet<Method>();
    for (final Method method : getMethodsAnnotatedWith(annotation)) {
      if (packages.contains(method.getDeclaringClass().getPackage().getName())) {
        results.add(method);
      }
    }
    return results;
  }

  public Set<Field> getFieldsAnnotatedWith(final Class<? extends Annotation> annotation, final Collection<String> packages) {
    final Set<Field> results = new HashSet<Field>();
    for (final Field field : getFieldsAnnotatedWith(annotation)) {
      if (packages.contains(field.getDeclaringClass().getPackage().getName())) {
        results.add(field);
      }
    }
    return results;
  }

  private Map<Class<? extends Annotation>, Set<Class<?>>> _annotationCache =
      new HashMap<Class<? extends Annotation>, Set<Class<?>>>();

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
    if (!annotationsToClassFile.containsKey(annotation.getName())) {
      return "0";
    }
    else {
      try {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");

        if (seed != null) {
          md.update(seed.getBytes());
        }

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (final SortableClassFileWrapper classFileWrapper : annotationsToClassFile.get(annotation.getName())) {
          byteArrayOutputStream.reset();
          final DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
          classFileWrapper.getClassFile().write(dataOutputStream);
          dataOutputStream.flush();
          md.update(byteArrayOutputStream.toByteArray());
        }

        return RebindUtils.hashToHexString(md.digest());

      }
      catch (Exception e) {
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

            if (properties.contains(ErraiAppAttribs.JUNIT_PACKAGE_ONLY)) {
              if ("true".equalsIgnoreCase(String.valueOf(properties.get(ErraiAppAttribs.JUNIT_PACKAGE_ONLY)))) {
                continue;
              }
            }
          }
          finally {
            stream.close();
          }
        }
        catch (IOException e) {
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
    }
    catch (IOException e) {
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
