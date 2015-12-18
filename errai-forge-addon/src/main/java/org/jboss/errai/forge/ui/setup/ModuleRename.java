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
import org.jboss.errai.forge.facet.aggregate.CoreFacet;
import org.jboss.errai.forge.facet.module.ModuleCoreFacet;
import org.jboss.errai.forge.ui.features.AddErraiFeatureCommand;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;

public class ModuleRename extends AbstractUICommand implements UIWizardStep {

  @Inject
  private ProjectHolder holder;

  @Inject
  private FacetFactory factory;

  @Inject
  @WithAttributes(label = "Enter a Short Name for the GWT Module", required = false, defaultValue = "",
          description = "This option can be used to give the module a shorter more convenient name than the logical name.")
  private UIInput<String> moduleName;

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(ModuleRename.class)
            .name("Rename Module")
            .description("You can give your module a more convenient name (such as app)."
                    + " Otherwise, the logical name will be used to reference the module in urls in the host page.");
  }

  @Override
  @SuppressWarnings("unchecked")
  public NavigationResult next(UINavigationContext context) throws Exception {
    return context.navigateTo(AddErraiFeatureCommand.class);
  }

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    builder.add(moduleName);
  }

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    final ProjectConfig projectConfig = holder.getProject().getFacet(ProjectConfig.class);
    final String logicalName = projectConfig.getProjectProperty(ProjectProperty.MODULE_LOGICAL, String.class);

    String newName = moduleName.getValue();
    if (newName == null || newName.equals("")) {
      newName = logicalName;
    }
    projectConfig.setProjectProperty(ProjectProperty.MODULE_NAME, newName);

    factory.install(holder.getProject(), CoreFacet.class);
    if (!newName.equals(logicalName))
      holder.getProject().getFacet(ModuleCoreFacet.class).setModuleName(newName);

    return Results.success();
  }

}
