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
