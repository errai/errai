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

package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiNavigationDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiNavigationModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiUiFacet.class, ErraiNavigationDependencyFacet.class,
    ErraiNavigationModuleFacet.class })
public class ErraiNavigationFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Navigation";
  }

  @Override
  public String getFeatureDescription() {
    return "Allows the use of Errai UI Templated Widgets as pages which can be navigated between.";
  }

  @Override
  public String getShortName() {
    return "navigation";
  }
}
