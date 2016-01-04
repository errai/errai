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

import org.jboss.errai.forge.facet.dependency.ErraiCdiServerDependencyFacet;
import org.jboss.errai.forge.facet.resource.BeansXmlFacet;
import org.jboss.errai.forge.facet.resource.CdiWebXmlFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * An aggregator facet for Errai CDI. This enables CDI events to be observed
 * between the client and server.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreFacet.class, ErraiCdiClientFacet.class, ErraiCdiServerDependencyFacet.class,
    CdiWebXmlFacet.class, BeansXmlFacet.class })
public class ErraiCdiFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai CDI Integration";
  }

  @Override
  public String getFeatureDescription() {
    return "Integrates server-side CDI Events and Observers with Errai Client CDI.";
  }

  @Override
  public String getShortName() {
    return "cdi";
  }
}
