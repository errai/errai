/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.cdi.async.test.bm.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent @LoadAsync
public class DependentBeanCycleB {
  @Inject DependentBeanCycleA dependentBeanCycleA;

  public static int instanceCount = 1;
  private int instance;
  private boolean preDestroy = false;

  @PostConstruct
  private void postConstruct() {
    instance = instanceCount++;
  }

  @PreDestroy
  public void preDestroy() {
    preDestroy = true;
  }

  public DependentBeanCycleA getDependentBeanCycleA() {
    return dependentBeanCycleA;
  }

  public int getInstance() {
    return instance;
  }

  public boolean isPreDestroy() {
    return preDestroy;
  }
}
