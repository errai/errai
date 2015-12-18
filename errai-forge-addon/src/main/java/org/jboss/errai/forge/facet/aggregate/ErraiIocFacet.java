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

import org.jboss.errai.forge.facet.dependency.ErraiIocDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiIocModulFacet;
import org.jboss.errai.forge.facet.resource.ErraiAppPropertiesFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/**
 * Aggregator facet for Errai IOC. Adds errai-ioc dependency and ensures
 * ErraiApp.properties is in resources folder.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FacetConstraint({ CoreFacet.class, ErraiIocDependencyFacet.class, ErraiIocModulFacet.class,
    ErraiAppPropertiesFacet.class })
public class ErraiIocFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai IOC";
  }

  @Override
  public String getFeatureDescription() {
    return "An IOC container allowing dependency injection in GWT-compiled code.";
  }

  @Override
  public String getShortName() {
    return "ioc";
  }
}
