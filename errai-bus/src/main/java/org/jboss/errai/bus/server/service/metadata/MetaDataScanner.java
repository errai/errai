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
package org.jboss.errai.bus.server.service.metadata;

import com.google.common.collect.ImmutableSet;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.Vfs;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

import static org.reflections.util.Utils.forNames;
import static org.reflections.vfs.Vfs.UrlType;

/**
 * Scans component meta data using javassist.
 * The scanner creates a {@link org.jboss.errai.bus.server.service.metadata.DeploymentContext}
 * that identifies nested subdeployments (i.e. WAR inside EAR) and processes the resulting archive Url's
 * using the <a href="http://code.google.com/p/reflections/">Reflections</a> library.
 *
 * <p/>
 * The initial set of config Url's (entry points) is discovered through through ErraiApp.properties.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 3, 2010
 */
public class MetaDataScanner extends Reflections {
  public static final String CLIENT_PKG_REGEX = "(.*?)(\\.client\\.)(.*?)";
  public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";

  MetaDataScanner(List<URL> urls) {
    super(new ConfigurationBuilder()
        .setUrls(urls)
        //.filterInputsBy(new FilterBuilder().exclude(CLIENT_PKG_REGEX))
        .setScanners(
        new FieldAnnotationsScanner(),
        new MethodAnnotationsScanner(),
        new TypeAnnotationsScanner(),
        new SubTypesScanner(),
        new ResourcesScanner()
    ));
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

  public static void main(String[] args) throws Exception
  {
    URL url = new URL("file:/Users/hbraun/dev/prj/errai/trunk/errai-bus/src/test/resources_metadata/hello_exp.war//WEB-INF/classes");
    String s = Vfs.normalizePath(url);
    System.out.println(s);
    boolean b = url.getProtocol().equals("file") && new java.io.File(s).isDirectory();
    System.out.println(b);
  }
}
