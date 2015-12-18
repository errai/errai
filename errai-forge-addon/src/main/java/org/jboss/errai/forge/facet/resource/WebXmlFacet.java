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

import org.jboss.errai.forge.facet.plugin.WarPluginFacet;

/**
 * This facet creates a web.xml file if none exists in the project.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class WebXmlFacet extends AbstractFileResourceFacet {
  
  private final String templateFile = "template-web.xml";

  @Override
  protected String getResourceContent() throws Exception {
    return readResource(templateFile).toString();
  }

  @Override
  public String getRelFilePath() {
    return WarPluginFacet.getWarSourceDirectory(getProject()) + "/WEB-INF/web.xml";
  }

}
