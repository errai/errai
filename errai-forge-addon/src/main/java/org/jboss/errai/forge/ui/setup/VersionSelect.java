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
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VersionSelect extends AbstractUICommand implements UIWizardStep {

  @Inject
  private ProjectHolder holder;

  @Inject
  @WithAttributes(label = "Select an Errai Version", required = true,
          requiredMessage = "An Errai version must be given to proceed.")
  private UISelectOne<String> versionSelect;

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(VersionSelect.class)
            .name("Select a version of Errai")
            .description("Note that the configuration used by this plugin is only compatible with Errai 3+.");
  }

  @Override
  @SuppressWarnings("unchecked")
  public NavigationResult next(UINavigationContext context) throws Exception {
    return context.navigateTo(ModuleSelect.class);
  }

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    versionSelect.setValueChoices(getValidErraiVersions());
    
    builder.add(versionSelect);
  }

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    final ProjectConfig projectConfig = holder.getProject().getFacet(ProjectConfig.class);
    projectConfig.setProjectProperty(ProjectProperty.ERRAI_VERSION, versionSelect.getValue());

    return Results.success();
  }

  private Collection<String> getValidErraiVersions() {
    final DependencyFacet depFacet = holder.getProject().getFacet(DependencyFacet.class);
    final Dependency erraiDep = DependencyBuilder.create(DependencyArtifact.ErraiParent.toString());

    final List<String> versions = new ArrayList<String>();
    for (final Coordinate coord : depFacet.resolveAvailableVersions(erraiDep)) {
      if (isValidVersion(coord.getVersion()))
        versions.add(coord.getVersion());
    }

    return versions;
  }

  private boolean isValidVersion(final String version) {
    final Double majorVersion;
    final Integer minorVersion;

    try {
      majorVersion = Double.valueOf(version.substring(0, 3));
      minorVersion = Integer.valueOf(version.substring(4, 5));
    }
    catch (NumberFormatException e) {
      return false;
    }

    /*
     * TODO: Change this depending on the latest valid Errai versions and snapshots released.
     */
    if (majorVersion == 3.0)
      return ((minorVersion >= 4) && (!version.contains("SNAPSHOT")));

    else if (majorVersion > 3.0)
      return ((minorVersion > 0) || (!version.contains("SNAPSHOT")));

    return false;
  }

}
