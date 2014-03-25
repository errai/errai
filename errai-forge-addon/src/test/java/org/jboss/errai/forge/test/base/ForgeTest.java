/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.forge.test.base;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.errai.forge.facet.plugin.BasePluginFacetTest;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class ForgeTest {

  public static final String DEPENDENCY = "org.jboss.errai.forge:errai-forge-addon";
  public static final String VERSION = "3.0-SNAPSHOT";
  public static final String FORGE_VERSION = "2.1.1.Final";
  public static final String ADDON_GROUP = "org.jboss.forge.addon";

  @Inject
  protected ProjectFactory projectFactory;

  @Inject
  protected FacetFactory facetFactory;

  @Deployment
  @Dependencies({
      @AddonDependency(name = DEPENDENCY, version = VERSION),
      @AddonDependency(name = ADDON_GROUP + ":projects", version = FORGE_VERSION),
      @AddonDependency(name = ADDON_GROUP + ":facets", version = FORGE_VERSION),
      @AddonDependency(name = ADDON_GROUP + ":maven", version = FORGE_VERSION)
  })
  public static ForgeArchive getDeployment() {
    final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClasses(
                    ForgeTest.class,
                    BasePluginFacetTest.class
                    )
            .addAsAddonDependencies(
                    AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi", FORGE_VERSION),
                    AddonDependencyEntry.create(DEPENDENCY, VERSION),
                    AddonDependencyEntry.create(ADDON_GROUP + ":projects", FORGE_VERSION),
                    AddonDependencyEntry.create(ADDON_GROUP + ":facets", FORGE_VERSION),
                    AddonDependencyEntry.create(ADDON_GROUP + ":maven", FORGE_VERSION)
            );

    return archive;
  }

  protected Project initializeJavaProject() {
    final Project project = projectFactory.createTempProject();

    return project;
  }

}
