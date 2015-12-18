/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.ui.command;

import org.jboss.errai.forge.facet.java.GwtMockitoRunnerFacet;
import org.jboss.errai.forge.facet.ui.command.res.SimpleTestableClass;
import org.jboss.errai.forge.facet.ui.command.res.UIExecutionContextMock;
import org.jboss.errai.forge.facet.ui.command.res.UIInputMock;
import org.jboss.errai.forge.test.base.ForgeTest;
import org.jboss.errai.forge.ui.command.CreateTestCommand;
import org.jboss.errai.forge.ui.command.CreateUnitTest;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.facets.MavenDependencyFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIContextProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class UnitTestCommandTest extends ForgeTest {

  private CreateTestCommand testableInstance;
  private Project project;
  private UIExecutionContextMock context;
  private UIInputMock<String> clazzNameInput;
  private UIInputMock<String> clazzInput;

  @Before
  public void setup() {
    project = initializeJavaProject();
    clazzInput = new UIInputMock<String>();
    clazzNameInput = new UIInputMock<String>();

    final String clazzName = "org.jboss.errai.forge.test.SimpleTestClass";
    clazzNameInput.setValue(clazzName);
    clazzInput.setValue(SimpleTestableClass.class.getName());

    testableInstance = new CreateUnitTest(null, facetFactory, clazzInput, clazzNameInput) {
      @Override
      protected Project getSelectedProject(UIContext context) {
        return project;
      }
      @Override
      protected Project getSelectedProject(UIContextProvider contextProvider) {
        return project;
      }
    };

    context = new UIExecutionContextMock();
  }

  @Test
  public void checkGeneratedTestClass() throws Exception {
    testableInstance.execute(context);

    final File testFile = new File(project.getRootDirectory().getUnderlyingResourceObject(),
            "src/test/java/org/jboss/errai/forge/test/SimpleTestClass.java");

    assertResourceAndFileContentsSame("org/jboss/errai/forge/test/SimpleTestClass.java", testFile);
  }
  
  @Test
  public void checkGwtRunnerFacetIsAdded() throws Exception {
    assertFalse(project.hasFacet(GwtMockitoRunnerFacet.class));
    
    testableInstance.execute(context);
    
    assertTrue(project.hasFacet(GwtMockitoRunnerFacet.class));
  }
  
  @Test
  public void checkTestPackageIsBlacklistedInGwtRunnerFacet() throws Exception {
    checkGwtRunnerFacetIsAdded();
    
    final GwtMockitoRunnerFacet runnerFacet = project.getFacet(GwtMockitoRunnerFacet.class);
    final Set<String> blacklistedPackages = runnerFacet.getBlacklistedPackages();
    
    assertEquals(2, blacklistedPackages.size());
    assertTrue(blacklistedPackages.contains("com.google.gwtmockito"));
    assertTrue(blacklistedPackages.contains("org.jboss.errai.forge.test"));
  }

  @Test
  public void checkTestDependenciesAdded() throws Exception {
    final MavenDependencyFacet mavenDependencyFacet = project.getFacet(MavenDependencyFacet.class);
    final DependencyBuilder mockitoDependency = DependencyBuilder.create("com.google.gwt.gwtmockito:gwtmockito");
    final DependencyBuilder junitDependency = DependencyBuilder.create("junit:junit");

    assertFalse(mavenDependencyFacet.hasDirectDependency(mockitoDependency));
    assertFalse(mavenDependencyFacet.hasDirectDependency(junitDependency));

    testableInstance.execute(context);

    assertTrue(mavenDependencyFacet.hasDirectDependency(mockitoDependency));
    assertTrue(mavenDependencyFacet.hasDirectDependency(junitDependency));
  }
  
  @Test
  public void checkTestDependenciesOnlyAddedOnce() throws Exception { 
    checkTestDependenciesAdded();
    final MavenDependencyFacet mavenDependencyFacet = project.getFacet(MavenDependencyFacet.class);
    final List<Dependency> dependenciesAfterSingleCommand = mavenDependencyFacet.getDependencies();
    
    testableInstance.execute(context);
    
    final List<Dependency> dependenciesAfterSecondCommand = mavenDependencyFacet.getDependencies();
    assertEquals(dependenciesAfterSingleCommand, dependenciesAfterSecondCommand);
  }

}
