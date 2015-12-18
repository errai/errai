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

import org.jboss.errai.forge.facet.dependency.ErraiCdiClientDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiCdiModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * An aggregator facet for errai-cdi-client. This enables client-side CDI
 * events.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ ErraiMessagingFacet.class, ErraiIocFacet.class, ErraiCdiClientDependencyFacet.class,
    ErraiCdiModuleFacet.class })
public class ErraiCdiClientFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Client-Side CDI";
  }

  @Override
  public String getFeatureDescription() {
    return "Allows the use of CDI Events and Observers in GWT-compiled code.";
  }

  @Override
  public String getShortName() {
    return "cdi-client";
  }
}
