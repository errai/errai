/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class InstanceTestBean {
  @Inject
  private Instance<ApplicationScopedBeanA> injectApplicationScoped;

  @Inject
  private Instance<DependentBeanA> injectDependentBeanA;

  @Inject
  private Instance<UnmanagedBean> unmanagedBean;

  @Inject
  private Instance<InterfaceA> ambiguousBean;

  public Instance<ApplicationScopedBeanA> getInjectApplicationScoped() {
    return injectApplicationScoped;
  }

  public Instance<DependentBeanA> getInjectDependentBeanA() {
    return injectDependentBeanA;
  }

  public Instance<UnmanagedBean> getUnmanagedBean() {
    return unmanagedBean;
  }

  public Instance<InterfaceA> getAmbiguousBean() {
    return ambiguousBean;
  }

}
