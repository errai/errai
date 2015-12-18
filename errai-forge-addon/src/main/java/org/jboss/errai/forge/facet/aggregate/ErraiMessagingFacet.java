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

import org.jboss.errai.forge.facet.dependency.ErraiBusDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiBusModuleFacet;
import org.jboss.errai.forge.facet.resource.ErraiBusServletConfigFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * Aggregator facet for Errai Messaging. Installing this facet adds the
 * necessary dependencies and web.xml configurations to immediately use the
 * Errai Message Bus in a project.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreFacet.class, ErraiBusDependencyFacet.class, ErraiBusModuleFacet.class,
    ErraiBusServletConfigFacet.class })
public class ErraiMessagingFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Messaging";
  }

  @Override
  public String getFeatureDescription() {
    return "Enables communication between services, either locally or between client and server,"
            + " through a simple Message Bus API.";
  }

  @Override
  public String getShortName() {
    return "messaging";
  }
}
