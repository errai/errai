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

package org.jboss.errai.forge.facet.dependency;

import org.jboss.errai.forge.util.VersionFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

import static org.jboss.errai.forge.constant.ArtifactVault.DependencyArtifact.*;
import static org.jboss.forge.addon.dependencies.builder.DependencyBuilder.create;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 * Contains the Jetty dependencies for the Maven integration-test profile
 */
@FacetConstraint(VersionFacet.class)
public class JettyIntegrationTestDependencyFacet extends AbstractDependencyFacet {

  public JettyIntegrationTestDependencyFacet() {
    setCoreDependencies();
    setProfileDependencies("integration-test", create(Jetty.toString()), create(JettyPlus.toString()), create(JettyNaming.toString()));
  }
}
