/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.Vfs;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

import static org.reflections.vfs.Vfs.UrlType;

/**
 * Scans component meta data using javassist.
 * The scanner creates a {@link DeploymentContext}
 * that identifies nested subdeployments (i.e. WAR inside EAR) and processes the resulting archive Url's
 * using the <a href="http://code.google.com/p/reflections/">Reflections</a> library.
 * <p/>
 * <p/>
 * The initial set of config Url's (entry points) is discovered through ErraiApp.properties.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 3, 2010
 */
public class MetaDataScanner extends Reflections {
  public static final String CLIENT_PKG_REGEX = ".*(\\.client\\.).*";
  public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";

  private static final PropertyScanner propScanner = new PropertyScanner(
          new Predicate<String>() {
            public boolean apply(String file) {
              return file.endsWith(".properties");
            }
          }
  );

  MetaDataScanner(List<URL> urls) {
    super(getConfiguration(urls));
    scan();
  }

  private static Configuration getConfiguration(List<URL> urls) {
    return new ConfigurationBuilder()
            .setUrls(urls)
                    //.filterInputsBy(new FilterBuilder().exclude(CLIENT_PKG_REGEX))
            .setScanners(
                    new FieldAnnotationsScanner(),
                    new MethodAnnotationsScanner(),
                    new TypeAnnotationsScanner(),
                    //new SubTypesScanner(),
                    propScanner
            );
  }

  public static MetaDataScanner createInstance() {
    return createInstance(getConfigUrls());
  }

  public static MetaDataScanner createInstance(List<URL> urls) {
    registerUrlTypeHandlers();

    DeploymentContext ctx = new DeploymentContext(urls);
    List<URL> actualUrls = ctx.process();
    MetaDataScanner scanner = new MetaDataScanner(actualUrls);
    ctx.close(); // needs to closed after the scanner was created

    return scanner;
  }

  private static void registerUrlTypeHandlers() {
    List<UrlType> urlTypes = Vfs.getDefaultUrlTypes();
    urlTypes.add(new VFSUrlType());
    urlTypes.add(new WARUrlType());

    // thread safe?
    Vfs.setDefaultURLTypes(urlTypes);
  }

  public Set<Class<?>> getTypesAnnotatedWithExcluding(
          Class<? extends Annotation> annotation, String excludeRegex) {
    Pattern p = Pattern.compile(excludeRegex);
    Set<String> result = new HashSet<String>();

    Set<String> types = getStore().getTypesAnnotatedWith(annotation.getName());
    for (String className : types) {
      if (!p.matcher(className).matches())
        result.add(className);
    }

    return ImmutableSet.copyOf(forNames(result));
  }

  public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation, String packagePrefix) {
    Set<Class<?>> results = new HashSet<Class<?>>();
    for (Class<?> cls : getTypesAnnotatedWith(annotation)) {
      if (packagePrefix == null || packagePrefix.length() == 0 || cls.getName().startsWith(packagePrefix)) {
        results.add(cls);
      }
    }
    return results;
  }


  public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation, String packagePrefix) {
    Set<Method> results = new HashSet<Method>();
    for (Method method : getMethodsAnnotatedWith(annotation)) {
      if (packagePrefix == null || packagePrefix.length() == 0 || method.getDeclaringClass().getName().startsWith
              (packagePrefix)
              ) {
        results.add(method);
      }
    }
    return results;
  }

  public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation, String packagePrefix) {
    Set<Field> results = new HashSet<Field>();
    for (Field field : getFieldsAnnotatedWith(annotation)) {
      if (packagePrefix == null || packagePrefix.length() == 0 || field.getDeclaringClass().getName()
              .startsWith(packagePrefix)) {
        results.add(field);
      }
    }
    return results;
  }

  public static List<URL> getConfigUrls(ClassLoader loader) {
    try {
      Enumeration<URL> configTargets = loader.getResources(ERRAI_CONFIG_STUB_NAME);

      List<URL> urls = new ArrayList<URL>();
      while (configTargets.hasMoreElements()) {
        String urlString = configTargets.nextElement().toExternalForm();
        urls.add(new URL(URLDecoder.decode(urlString.substring(0, urlString.indexOf(ERRAI_CONFIG_STUB_NAME)), "utf-8")));
      }
      return urls;
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to scan configuration Url's", e);
    }
  }

  public static List<URL> getConfigUrls() {
    return getConfigUrls(MetaDataScanner.class.getClassLoader());
  }

  public Properties getProperties(String name) {
    return propScanner.getProperties().get(name);
  }
}
