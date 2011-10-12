/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.bus.tests;

import junit.framework.TestCase;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.common.metadata.MetaDataScanner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Test the VFS extensions for Reflections and verify it can read from ear files.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 5, 2010
 */
public class PackageScanTest extends TestCase {
  private static String getPackageResourcePath() {
    URL url = PackageScanTest.class.getClassLoader().getResource("ErraiApp.properties");
    if (url == null) throw new RuntimeException("Can't find the resource path for the test!");

    File curr = new File(url.getFile());
    File parent = curr.getParentFile().getParentFile();

    if (parent.getName().endsWith("target")) {
      parent = parent.getParentFile();
      parent = new File(parent + "/src/test/");
    }

    File resourcesMetadata = new File(parent.getPath() + "/resources_metadata");

    if (!resourcesMetadata.exists())
      throw new RuntimeException("Can't find the resource path for the test: " + resourcesMetadata.getPath());

    return resourcesMetadata.getPath();
  }

  public void testEarScan() throws Exception {
    File ear = new File(getPackageResourcePath() + "/helloworld.ear");
    assertTrue(ear.exists());
    URL earUrl = new URL(ear.toURI().toString() + "/helloworld.war!/WEB-INF/classes");
    URL libUrl = new URL(ear.toURI().toString() + "/helloworld.war!/WEB-INF/lib/errai-tools-1.1-SNAPSHOT.jar");

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(earUrl);
    urlList.add(libUrl);

    MetaDataScanner scanner = createScanner(urlList);

    // nested in ear/war/WEB-INF/classes
    Set<String> classesMeta = scanner.getStore().getTypesAnnotatedWith(ApplicationComponent.class.getName());
    assertFalse("Cannot find @ApplicationComponent on HelloWorldService", classesMeta.isEmpty());

    // nested in ear/war/WEB-INF/lib
    Set<String> libMeta = scanner.getStore().getTypesAnnotatedWith(ExtensionComponent.class.getName());
    boolean match = false;
    for (String className : libMeta) {
      if ("org.jboss.errai.tools.monitoring.MonitorExtension".equals(className)) {
        match = true;
        break;
      }
    }

    assertTrue("Cannot find @ExtensionComponent on MonitorExtension", match);
  }

  public void testWarScan() throws Exception {
    File war = new File(getPackageResourcePath() + "/helloworld.war");
    assertTrue(war.exists());
    URL warUrl = war.toURI().toURL();

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(warUrl);
    MetaDataScanner scanner = createScanner(urlList);

    Set<String> annotated = scanner.getStore().getTypesAnnotatedWith(ApplicationComponent.class.getName());
    assertFalse("Cannot find @ApplicationComponent on HelloWorldService", annotated.isEmpty());
  }

  public void testExplodedWarScan() throws Exception {
    File war = new File(getPackageResourcePath() + "/hello_exp.war");
    assertTrue(war.exists());
    URL warUrl = new URL(war.toURI().toURL() + "/WEB-INF/classes");

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(warUrl);
    MetaDataScanner scanner = createScanner(urlList);

    Set<String> annotated = scanner.getStore().getTypesAnnotatedWith(ApplicationComponent.class.getName());
    assertFalse("Cannot find @ApplicationComponent on HelloWorldService", annotated.isEmpty());
  }

  private MetaDataScanner createScanner(List<URL> urlList) {
    long s0 = System.currentTimeMillis();
    MetaDataScanner scanner = MetaDataScanner.createInstance(urlList);
    System.out.println("Scan time: " + (System.currentTimeMillis() - s0));
    return scanner;

  }
}
