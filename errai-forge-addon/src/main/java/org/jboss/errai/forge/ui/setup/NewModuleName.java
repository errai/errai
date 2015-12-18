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

public class NewModuleName extends AbstractUICommand implements UIWizardStep {

  private static final String DESCRIPTION =
          "The logical name of GWT module is the fully-qualified package"
                  + " and the name of the module file (without the gwt.xml suffix)."
                  + " For example, the module file org/jboss/errai/App.gwt.xml would"
                  + " have the logical name org.jboss.errai.App";

  @Inject
  private ProjectHolder holder;

  @Inject
  @WithAttributes(label = "Enter a Logical Module Name",
          required = true, description = DESCRIPTION)
  private UIInput<String> logicalModuleName;

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata
            .forCommand(NewModuleName.class)
            .name("Enter the logical name for your new GWT module")
            .description(
                    "A module's logical name looks like fully qualified Java class name (for example: org.jboss.errai.App)."
                            + " The module's package will be the root package of your GWT/Errai application.");
  }

  @Override
  @SuppressWarnings("unchecked")
  public NavigationResult next(UINavigationContext context) throws Exception {
    return context.navigateTo(ModuleRename.class);
  }

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    builder.add(logicalModuleName);
  }

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    final ProjectConfig projectConfig = holder.getProject().getFacet(ProjectConfig.class);
    projectConfig.setProjectProperty(ProjectProperty.MODULE_LOGICAL, logicalModuleName.getValue());
    projectConfig.setProjectProperty(ProjectProperty.MODULE_FILE,
            ModuleSelect.moduleLogicalNameToFile(logicalModuleName.getValue(), holder.getProject()));

    return Results.success();
  }

}
