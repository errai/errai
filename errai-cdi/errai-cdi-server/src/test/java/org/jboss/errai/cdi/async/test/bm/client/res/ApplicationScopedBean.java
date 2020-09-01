/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.async.test.bm.client.res;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.ioc.client.api.LoadAsync;

import com.google.gwt.user.client.Random;

/**
 * @author Mike Brock
 */
@ApplicationScoped @LoadAsync(DependentScopedBean.class)
public class ApplicationScopedBean {
  @Inject DependentScopedBean bean1;
  @Inject DependentScopedBean bean2;

  @Inject ClientMessageBus clientMessageBus;

  // the dependent qualification is optional -- this just confirms it doesn't break when used.
  @Inject @Dependent DependentScopedBean bean3;
  
  @Inject @Dependent DependentScopedBeanWithDependencies beanWithDependencies;

  private boolean preDestroyCalled = false;

  private static int counter = 0;

  private int beanId = ++counter * Random.nextInt();

  public DependentScopedBean getBean1() {
    return bean1;
  }

  public DependentScopedBean getBean2() {
    return bean2;
  }

  public DependentScopedBean getBean3() {
    return bean3;
  }

  public DependentScopedBeanWithDependencies getBeanWithDependencies() {
    return beanWithDependencies;
  }

  @PreDestroy
  private void preDestroy() {
    preDestroyCalled = true;
  }

  public boolean isPreDestroyCalled() {
    return preDestroyCalled;
  }

  public int getBeanId() {
    return beanId;
  }
}
