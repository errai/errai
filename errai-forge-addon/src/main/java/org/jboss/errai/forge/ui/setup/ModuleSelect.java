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

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.constant.DefaultVault.DefaultValue;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ModuleSelect extends AbstractUICommand implements UIWizardStep {

  private static final String CREATE_A_NEW_MODULE_GUI = "Create a new module...";
  private static final String CREATE_A_NEW_MODULE_CLI = "create-new";

  @Inject
  private ProjectHolder holder;

  @Inject
  @WithAttributes(label = "Select a GWT module for Errai", required = true)
  private UISelectOne<String> moduleSelect;

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(ModuleSelect.class)
            .name("Select a GWT Module")
            .description("Select the GWT Module which will inherit all Errai GWT dependencies for your application.");
  }

  private boolean isCreateNew(final String value) {
    return CREATE_A_NEW_MODULE_CLI.equals(value) || CREATE_A_NEW_MODULE_GUI.equals(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public NavigationResult next(UINavigationContext context) throws Exception {
    if (moduleSelect.getValue() == null) {
      return null;
    }
    else if (isCreateNew(moduleSelect.getValue())) {
      return context.navigateTo(NewModuleName.class);
    }
    else {
      return context.navigateTo(ModuleRename.class);
    }
  }

  @Override
  public void initializeUI(UIBuilder builder) throws Exception {
    final List<String> choices = getExistingModules();
    // Workaround FORGE-1639
    final boolean isGui = builder.getUIContext().getProvider().isGUI();
    final String createNew = (isGui) ? CREATE_A_NEW_MODULE_GUI : CREATE_A_NEW_MODULE_CLI;

    choices.add(createNew);
    moduleSelect.setValueChoices(choices);
    moduleSelect.setDefaultValue(createNew);

    builder.add(moduleSelect);
  }

  @Override
  public Result execute(UIExecutionContext context) throws Exception {
    if (!isCreateNew(moduleSelect.getValue())) {
      final ProjectConfig projectConfig = holder.getProject().getFacet(ProjectConfig.class);
      projectConfig.setProjectProperty(ProjectProperty.MODULE_LOGICAL, moduleSelect.getValue());
      projectConfig.setProjectProperty(ProjectProperty.MODULE_FILE,
              moduleLogicalNameToFile(moduleSelect.getValue(), holder.getProject()));
    }

    return Results.success();
  }

  private static Collection<File> findGwtModuleFiles(final File f) {
    if (f.exists()) {
      if (f.isDirectory()) {
        Collection<File> retVal = new LinkedList<File>();
        for (final File child : f.listFiles()) {
          final Collection<File> result = findGwtModuleFiles(child);
          if (result.size() > 0) {
            retVal.addAll(result);
          }
        }

        return retVal;
      }
      else if (f.isFile()) {
        if (f.getName().endsWith(".gwt.xml")) {
          final Collection<File> retVal = new LinkedList<File>();
          retVal.add(f);

          return retVal;
        }
      }
    }

    return new LinkedList<File>();
  }

  private List<String> getExistingModules() {
    final File dir = getSourceFolder();

    final List<String> retVal = new ArrayList<String>();

    if (dir.exists()) {
      final Collection<File> found = findGwtModuleFiles(dir);
      for (File file : found) {
        String relPath = file.getAbsolutePath().replace(dir.getAbsolutePath(), "");
        if (relPath.charAt(0) == File.separatorChar)
          relPath = relPath.substring(1);

        retVal.add(relPath.replace(File.separatorChar, '.').replaceFirst("\\.gwt\\.xml$", ""));
      }
    }

    return retVal;
  }

  private File getSourceFolder() {
    final MavenFacet mavenFacet = holder.getProject().getFacet(MavenFacet.class);
    final Model model = mavenFacet.getModel();

    Build build = model.getBuild();
    if (build == null) {
      build = new Build();
      model.setBuild(build);
    }

    String srcDir = build.getSourceDirectory();

    if (srcDir == null || srcDir.equals("")) {
      srcDir = DefaultValue.SourceDirectory.getDefaultValue();
    }

    return new File(holder.getProject().getRootDirectory().getUnderlyingResourceObject(), srcDir);
  }

  public static File moduleLogicalNameToFile(final String moduleName, final Project project) {
    final String relModuleFile = moduleName.replace('.', File.separatorChar) + ".gwt.xml";
    final MavenFacet coreFacet = project.getFacet(MavenFacet.class);
    final Build build = coreFacet.getModel().getBuild();

    final String relSrcRoot = (build == null || build.getSourceDirectory() == null) ?
            DefaultValue.SourceDirectory.getDefaultValue() :
            build.getSourceDirectory();

    final File modulePath = new File(new File(project.getRootDirectory().getUnderlyingResourceObject(), relSrcRoot),
            relModuleFile);

    return modulePath;
  }

}
