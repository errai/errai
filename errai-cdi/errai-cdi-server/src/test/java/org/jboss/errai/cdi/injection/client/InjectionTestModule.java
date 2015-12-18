/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see InjectionIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@EntryPoint
public class InjectionTestModule {
  private boolean postConstructFired;

  private InjectionDependentTestBeanA beanA;

  private InjectionDependentTestBeanC beanC;

  // TODO make decision regarding support of @New
  @Inject
//  @New
  private InjectionDependentTestBeanC beacC1;

  // test public mutable field injection -- not that this is a terribly good idea.
  @Inject
  public SomeRandomBeanToInject randomBeanToInject;

  @PostConstruct
  public void doPostConstruct() {
    postConstructFired = true;
  }

  // test public method injection
  @Inject
  public void setBeanA(InjectionDependentTestBeanA beanA) {
    this.beanA = beanA;
  }

  // test private method injection
  @Inject
  private void setBeanC(InjectionDependentTestBeanC beanC) {
    this.beanC = beanC;
  }

  public InjectionDependentTestBeanA getBeanA() {
    return beanA;
  }

  public InjectionDependentTestBeanC getBeanC() {
    return beanC;
  }

  public InjectionDependentTestBeanC getBeanC1() {
    return beacC1;
  }

  public SomeRandomBeanToInject getRandomBeanToInject() {
    return randomBeanToInject;
  }

  public boolean isPostConstructFired() {
    return postConstructFired;
  }
}
