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

package org.jboss.errai.ioc.support.bus.tests.client.res;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.container.IOC;

@Dependent
public class FirstAfterInitBean {

  private boolean afterInitCalled;
  private SecondAfterInitBean bean;

  @AfterInitialization
  public void aferInit() {
    afterInitCalled = true;
    bean = IOC.getBeanManager().lookupBean(SecondAfterInitBean.class).getInstance();
  }

  public boolean isAfterInitCalled() {
    return afterInitCalled;
  }
  
  public SecondAfterInitBean getSecondAfterInitBean() {
    return bean;
  }
}
