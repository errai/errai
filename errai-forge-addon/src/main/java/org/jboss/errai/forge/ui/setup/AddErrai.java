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

package org.jboss.errai.forge.ui.setup;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.util.Condition;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

public class AddErrai extends AbstractProjectCommand implements UIWizard {

  @Inject
  private ProjectFactory projectFactory;
  
  @Inject
  private FacetFactory facetFactory;
  
  @Inject
  private ProjectHolder holder;
  
  @SuppressWarnings("unchecked")
  private final Condition<Project>[] conditions = new Condition[] {
      new Condition<Project>() {
        @Override
        public boolean isSatisfied(final Project subject) {
          return subject.hasFacet(MavenFacet.class);
        }

        @Override
        public String getShortDescription() {
          return "The given project must be a Maven project.";
        }
      }
  };
  
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(AddErrai.class)
            .name("Errai: Setup")
            .category(Categories.create("Project", "Errai"))
            .description("Setup base configuration for developing a GWT/Errai Application.");
  };

  @Override
  public Result execute(final UIExecutionContext context) throws Exception {
    final Result result = verifyProject(holder.getProject());
    
    if (!(result instanceof Failed)) {
      facetFactory.install(holder.getProject(), ProjectConfig.class);
    }
    
    return result;
  }

  @Override
  public void initializeUI(final UIBuilder builder) throws Exception {
    final Project selectedProject = getSelectedProject(builder.getUIContext());
    holder.setProject(selectedProject);
  }
  
  private Result verifyProject(final Project project) {
    final Collection<String> problems = getProblems(project);

    if (problems.isEmpty()) {
      return Results.success();
    }
    else {
      final StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append(String.format(
              "There are some problems with your project (%s) preventing the installation of Errai with this Addon.",
              project.getRootDirectory().getUnderlyingResourceObject().getAbsolutePath()));
      for (final String problem : problems) {
        messageBuilder.append(problem);
      }
      
      return Results.fail(messageBuilder.toString());
    }
  }
  
  private Collection<String> getProblems(final Project project) {
    final Collection<String> problems = new ArrayList<String>();

    for (int i = 0; i < conditions.length; i++) {
      if (!conditions[i].isSatisfied(project))
        problems.add(conditions[i].getShortDescription());
    }

    return problems;
  }

  @Override
  @SuppressWarnings("unchecked")
  public NavigationResult next(UINavigationContext context) throws Exception {
    if (holder.getProject() != null) {
      return context.navigateTo(VersionSelect.class);
    }
    else {
      return null;
    }
  }

  @Override
  protected boolean isProjectRequired() {
    return true;
  }

  @Override
  protected ProjectFactory getProjectFactory() {
    return projectFactory;
  }

}
