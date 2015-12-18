/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.java;

import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.forge.addon.projects.Project;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import static org.junit.Assert.*;

public class TestGwtMockitoRunnerFacet extends ForgeTest {

  private Project project;
  private GwtMockitoRunnerFacet testFacet;

  @Before
  public void setup() {
    project = createErraiTestProject();
    testFacet = facetFactory.install(project, GwtMockitoRunnerFacet.class);
  }

  @Test
  public void addSingleBlacklistedPackage() throws Exception {
    assertInstalled();
    
    assertTrue(testFacet.addBlacklistedPackage("org.jboss.errai"));
    final Set<String> blacklistedPackages = testFacet.getBlacklistedPackages();
    assertEquals(2, blacklistedPackages.size());
    assertTrue(blacklistedPackages.contains("org.jboss.errai"));
    assertTrue(blacklistedPackages.contains("com.google.gwtmockito"));
    
    try (final InputStreamReader templateReader = new InputStreamReader(
            ClassLoader
                    .getSystemResourceAsStream("org/jboss/errai/forge/facet/java/GwtMockitoRunnerExtensionOneAdded.java"));
         final InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                    testFacet.getAbsoluteFilePath()))) {
      assertFileContentsSame(templateReader, fileReader);
    }
  }
  
  @Test
  public void addTwoBlacklistedPackages() throws Exception {
    assertInstalled();
    
    assertTrue(testFacet.addBlacklistedPackage("org.jboss.errai"));
    assertTrue(testFacet.addBlacklistedPackage("com.google.gwt"));

    final Set<String> blacklistedPackages = testFacet.getBlacklistedPackages();
    assertEquals(3, blacklistedPackages.size());
    assertTrue(blacklistedPackages.contains("org.jboss.errai"));
    assertTrue(blacklistedPackages.contains("com.google.gwt"));
    assertTrue(blacklistedPackages.contains("com.google.gwtmockito"));
    
    try (final InputStreamReader templateReader = new InputStreamReader(
            ClassLoader
                    .getSystemResourceAsStream("org/jboss/errai/forge/facet/java/GwtMockitoRunnerExtensionTwoAdded.java"));
         final InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                    testFacet.getAbsoluteFilePath()))) {
      assertFileContentsSame(templateReader, fileReader);
    }
  }
  
  @Test
  public void addAndRemoveBlacklistedPackages() throws Exception {
    addTwoBlacklistedPackages();
    
    assertTrue(testFacet.removeBlacklistedPackage("com.google.gwt"));
    assertTrue(testFacet.removeBlacklistedPackage("org.jboss.errai"));
    
    final Set<String> blacklistedPackages = testFacet.getBlacklistedPackages();
    assertEquals(1, blacklistedPackages.size());
    assertTrue(blacklistedPackages.contains("com.google.gwtmockito"));
    
    assertInstalled();
  }
  
  @Test
  public void testBlacklistedPacakgeNotAddedTwice() throws Exception {
    addSingleBlacklistedPackage();
    assertFalse(testFacet.addBlacklistedPackage("org.jboss.errai"));

    final Set<String> blacklistedPackages = testFacet.getBlacklistedPackages();
    assertEquals(2, blacklistedPackages.size());
    assertTrue(blacklistedPackages.contains("com.google.gwtmockito"));
    assertTrue(blacklistedPackages.contains("org.jboss.errai"));
    
    try (final InputStreamReader templateReader = new InputStreamReader(
            ClassLoader
                    .getSystemResourceAsStream("org/jboss/errai/forge/facet/java/GwtMockitoRunnerExtensionOneAdded.java"));
         final InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                    testFacet.getAbsoluteFilePath()))) {
      assertFileContentsSame(templateReader, fileReader);
    }
  }

  private void assertInstalled() throws IOException, FileNotFoundException {
    assertTrue(project.hasFacet(GwtMockitoRunnerFacet.class));
    assertTrue(project.getFacet(GwtMockitoRunnerFacet.class).isInstalled());
    try (final InputStreamReader templateReader = new InputStreamReader(
            ClassLoader
                    .getSystemResourceAsStream("org/jboss/errai/forge/facet/java/GwtMockitoRunnerExtensionBase.java"));
         final InputStreamReader fileReader = new InputStreamReader(new FileInputStream(
                    testFacet.getAbsoluteFilePath()))) {
      assertFileContentsSame(templateReader, fileReader);
    }
  }

}
