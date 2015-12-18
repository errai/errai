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

package org.jboss.errai.forge.facet.resource;

import org.jboss.errai.forge.config.ProjectConfig;
import org.jboss.errai.forge.config.ProjectProperty;
import org.jboss.errai.forge.facet.plugin.WarPluginFacet;

import java.io.File;

/**
 * This facet adds a GWT host page if none already exists.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class GwtHostPageFacet extends AbstractFileResourceFacet {

  private final String templateName = "host_page_template.html";
  private final String FILLER_VALUE = "$$_MODULE_JS_FILE_$$";

  @Override
  protected String getResourceContent() throws Exception {
    final StringBuilder builder = readResource(templateName);
    final ProjectConfig config = getProject().getFacet(ProjectConfig.class);
    final String moduleName = config.getProjectProperty(ProjectProperty.MODULE_NAME, String.class);
    // Replace filler with actual module js file
    replace(builder, FILLER_VALUE, getJsFilePath(moduleName));

    return builder.toString();
  }

  private String getJsFilePath(final String moduleName) {
    return moduleName + File.separator + moduleName + ".nocache.js";
  }

  @Override
  public String getRelFilePath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/index.html";
  }

}
