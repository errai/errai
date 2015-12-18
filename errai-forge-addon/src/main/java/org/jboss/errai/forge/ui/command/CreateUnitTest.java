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

package org.jboss.errai.forge.ui.command;

import org.jboss.errai.forge.facet.aggregate.CoreFacet;
import org.jboss.errai.forge.facet.java.GwtMockitoRunnerFacet;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.GwtMockito;
import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.JUnit;

@FacetConstraint({ CoreFacet.class })
public class CreateUnitTest extends CreateTestCommand {
  
  @Inject
  private FacetFactory facetFactory;

  @Inject
  private UIInput<String> testableClassName;
  @Inject
  private UIInput<String> testClassName;
  
  private TemplateWriter templateWriter;
  
  public CreateUnitTest() {
    templateWriter = new TemplateWriter(
            "/org/jboss/errai/forge/ui/command/TestClassTemplate.java",
            "$$_testClassPackage_$$",
            "$$_testClassSimpleName_$$",
            "$$_testableClassFullName_$$",
            "$$_testableClassSimpleName_$$");
  }

  public CreateUnitTest(final ProjectFactory projectFactory, final FacetFactory facetFactory,
          final UIInput<String> testableClazzName, final UIInput<String> testClazzName) {
    this();
    this.projectFactory = projectFactory;
    this.facetFactory = facetFactory;
    this.testableClassName = testableClazzName;
    this.testClassName = testClazzName;
  }
  
  @Override
  public void initializeUI(final UIBuilder builder) throws Exception {
    testableClassName.setLabel("Class to Test");
    testableClassName.setDescription("The fully-qualified name of the class you would like to test.");
    testableClassName.setRequired(true);
    testableClassName.setRequiredMessage("You must provide the name of a class to test.");
    
    testClassName.setLabel("Test Class Name");
    testClassName.setDescription("The fully-qualified name of the test class to create.");
    testClassName.setRequired(true);
    testableClassName.setRequiredMessage("You must provide the name of for the test class.");
    testClassName.setDefaultValue(new Callable<String>() {
      @Override
      public String call() throws Exception {
        if (testableClassName.hasValue()) {
          return testableClassName.getValue() + "UnitTest";
        }

        return null;
      }
    });

    builder.add(testableClassName).add(testClassName);
  }

  @Override
  public Result execute(final UIExecutionContext context) throws Exception {
    final Project project = getSelectedProject(context.getUIContext());
    
    configureGwtMockitoRunner(project);
    produceTestFile(project);
    addTestDependencies(project);
    
    return Results.success();
  }

  private void configureGwtMockitoRunner(final Project project) {
    final GwtMockitoRunnerFacet runnerFacet;
    if (!project.hasFacet(GwtMockitoRunnerFacet.class)) {
      runnerFacet = facetFactory.install(project, GwtMockitoRunnerFacet.class);
    }
    else {
      runnerFacet = project.getFacet(GwtMockitoRunnerFacet.class);
    }
    
    final String testPackage = getPackage(testClassName.getValue());
    runnerFacet.addBlacklistedPackage(testPackage);
  }

  private void addTestDependencies(final Project project) {
    addTestScopedDependency(project, GwtMockito);
    addTestScopedDependency(project, JUnit);
  }
  
  private void produceTestFile(final Project project) throws IOException {
    final String testableClassName = this.testableClassName.getValue();
    final String testClassName = this.testClassName.getValue();

    final File sourceDirectory = getTestSourceDirectory(project);

    final File testFile = new File(sourceDirectory, relativeFilePathFromClassName(testClassName));

    templateWriter.set("$$_testClassPackage_$$", getPackage(testClassName))
            .set("$$_testClassSimpleName_$$", getSimpleName(testClassName))
            .set("$$_testableClassFullName_$$", testableClassName)
            .set("$$_testableClassSimpleName_$$", getSimpleName(testableClassName)).writeTemplate(testFile);
  }

  @Override
  protected String getCommandName() {
    return "Add Unit Test";
  }

  @Override
  protected String getCommandDescription() {
    return "Create a gwt-mockito unit test.";
  }

}
