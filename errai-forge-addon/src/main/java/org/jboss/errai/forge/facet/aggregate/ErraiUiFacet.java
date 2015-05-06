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
package org.jboss.errai.forge.facet.aggregate;

import org.jboss.errai.forge.facet.dependency.ErraiDataBindingDependencyFacet;
import org.jboss.errai.forge.facet.dependency.ErraiUiDependencyFacet;
import org.jboss.errai.forge.facet.dependency.ErraiWeldIntegrationDependencyFacet;
import org.jboss.errai.forge.facet.module.ErraiUiModuleFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;

/*
 * NOTE: We must depend on the ErraiDataBindingDependencyFacet because the errai
 * ui module inherits from the databinding module, but the errai ui does not
 * have a dependency (via maven) on databinding.
 */

@FacetConstraint({ CoreFacet.class, ErraiIocFacet.class, ErraiCdiClientFacet.class, ErraiUiDependencyFacet.class,
    ErraiDataBindingDependencyFacet.class, ErraiUiModuleFacet.class, ErraiWeldIntegrationDependencyFacet.class })
public class ErraiUiFacet extends BaseAggregatorFacet {

  @Override
  public String getFeatureName() {
    return "Errai UI";
  }

  @Override
  public String getFeatureDescription() {
    return "Create your own custom Widgets using HTML5 templates. "
            + "Elements in the template can be bound to fields in Java classes with a simple declarative syntax.";
  }

  @Override
  public String getShortName() {
    return "ui";
  }
}
