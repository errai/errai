/*
 * Copyright (C) 2010 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.tests;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.tests.support.FunAnnotatedClientClass;
import org.jboss.errai.bus.client.tests.support.FunAnnotatedClientClass2;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.google.common.io.Files;

/**
 * Test the VFS extensions for Reflections and verify it can read from ear files.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>>
 * @author Christian Sadilek <csadilek@redhat.com>>
 * @date: Aug 5, 2010
 */
public class PackageScanTest extends TestCase {

  public void testEarScan() throws Exception {
    File ear = File.createTempFile("helloworld", ".ear");

    JavaArchive jarArchive = ShrinkWrap.create(JavaArchive.class, "my-lib-1.0.jar")
        .addClass(FunAnnotatedClientClass2.class);
    WebArchive warArchive = ShrinkWrap.create(WebArchive.class, "helloworld.war")
        .addClasses(FunAnnotatedClientClass.class)
        .addAsLibrary(jarArchive);
    EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class)
        .addAsModule(warArchive);
    earArchive.as(ZipExporter.class).exportTo(ear, true);

    URL earUrl = new URL(ear.toURI() + "/helloworld.war!/WEB-INF/classes");
    URL libUrl = new URL(ear.toURI() + "/helloworld.war!/WEB-INF/lib/my-lib-1.0.jar");

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(earUrl);
    urlList.add(libUrl);

    MetaDataScanner scanner = createScanner(urlList);

    // nested in ear/war/WEB-INF/classes
    assertTrue("Didn't find @Local annotated class FunAnnotatedClientClass in ear-war nesting",
        scanner.getStore().getTypesAnnotatedWith(Local.class.getName())
            .contains("org.jboss.errai.bus.client.tests.support.FunAnnotatedClientClass"));

    // nested in ear/war/WEB-INF/lib
    assertTrue("Didn't find @Service annotated class FunAnnotatedClientClass2 in ear-war-jar nesting",
        scanner.getStore().getTypesAnnotatedWith(Service.class.getName())
            .contains("org.jboss.errai.bus.client.tests.support.FunAnnotatedClientClass2"));
  }

  public void testWarScan() throws Exception {
    final File war = File.createTempFile("test", ".war");
    WebArchive archive = ShrinkWrap.create(WebArchive.class)
         .addClasses(FunAnnotatedClientClass.class);
    archive.as(ZipExporter.class).exportTo(war, true);

    assertTrue(war.exists());
    URL warUrl = war.toURI().toURL();

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(warUrl);
    MetaDataScanner scanner = createScanner(urlList);

    String annotationToSearchFor = Local.class.getName();
    Set<String> annotated = scanner.getStore().getTypesAnnotatedWith(annotationToSearchFor);
    assertFalse("Cannot find " + annotationToSearchFor + " in " + war, annotated.isEmpty());
    war.delete();
  }

  public void testExplodedWarScan() throws Exception {
    final File warParentDir = Files.createTempDir();
    assertTrue(warParentDir.isDirectory());

    WebArchive archive = ShrinkWrap.create(WebArchive.class, "explode-me")
         .addClasses(FunAnnotatedClientClass.class);
    archive.as(ExplodedExporter.class).exportExploded(warParentDir);

    File warBaseDir = new File(warParentDir, "explode-me");
    assertTrue("Missing exploded war at " + warBaseDir, new File(warBaseDir, "WEB-INF").isDirectory());

    URL warUrl = new URL(warBaseDir.toURI().toURL() + "/WEB-INF/classes");

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(warUrl);
    MetaDataScanner scanner = createScanner(urlList);

    String annotationToSearchFor = Local.class.getName();
    Set<String> annotated = scanner.getStore().getTypesAnnotatedWith(annotationToSearchFor);
    assertFalse("Cannot find " + annotationToSearchFor + " in " + warBaseDir, annotated.isEmpty());
    FileUtils.deleteDirectory(warParentDir);
    warParentDir.delete();
  }

  private MetaDataScanner createScanner(List<URL> urlList) {
    long s0 = System.currentTimeMillis();
    MetaDataScanner scanner = MetaDataScanner.createInstance(urlList, null);
    System.out.println("Scan time: " + (System.currentTimeMillis() - s0));
    return scanner;

  }
}
