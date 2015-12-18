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

import org.jboss.errai.forge.facet.dependency.ErraiJpaDatasyncDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiJpaDatasyncModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiJpaClientFacet.class, ErraiCdiClientFacet.class,
    ErraiJpaDatasyncDependencyFacet.class, ErraiJpaDatasyncModuleFacet.class })
public class ErraiJpaDatasyncFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai JPA Datasync";
  }

  @Override
  public String getFeatureDescription() {
    return "Synchronize client-side persisted data with server-side JPA.";
  }

  @Override
  public String getShortName() {
    return "jpa-datasync";
  }
}
