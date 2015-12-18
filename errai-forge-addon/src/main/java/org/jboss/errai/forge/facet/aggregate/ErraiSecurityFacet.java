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

import org.jboss.errai.forge.facet.dependency.ErraiSecurityDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiSecurityModuleFacet;
import org.jboss.errai.forge.facet.resource.SecurityBeansXmlFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

@FacetConstraint({ CoreFacet.class, ErraiMessagingFacet.class, ErraiIocFacet.class, ErraiUiFacet.class,
    ErraiNavigationFacet.class, ErraiCdiFacet.class, ErraiSecurityDependencyFacet.class,
    ErraiSecurityModuleFacet.class, SecurityBeansXmlFacet.class })
public class ErraiSecurityFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai Security";
  }

  @Override
  public String getShortName() {
    return "errai-security";
  }

  @Override
  public String getFeatureDescription() {
    return "A module providing support for security features such as"
            + " page redirection and secure RPC services.";
  }

}
