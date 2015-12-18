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

import org.jboss.errai.forge.facet.dependency.ErraiJpaClientDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiJpaClientModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiIocFacet.class, ErraiDataBindingFacet.class,
    ErraiJpaClientDependencyFacet.class, ErraiJpaClientModuleFacet.class })
public class ErraiJpaClientFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai JPA Client";
  }

  @Override
  public String getFeatureDescription() {
    return "An implementation of JPA for GWT.";
  }

  @Override
  public String getShortName() {
    return "jpa-client";
  }
}
