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

package org.jboss.errai.cdi.async.test.cyclic.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.LoadAsync;

/**
 * @author Mike Brock
 */
@ApplicationScoped @LoadAsync
public class ApplicationScopedBeanInjectSelf {
  private static int counter = 0;
  private int instance = ++counter;
  private ApplicationScopedBeanInjectSelf self;

  // required to make proxyable
  public ApplicationScopedBeanInjectSelf() {
  }

  @Inject
  public ApplicationScopedBeanInjectSelf(ApplicationScopedBeanInjectSelf selfRefProxy) {
    this.self = selfRefProxy;
  }

  public int getInstance() {
    return instance;
  }

  public ApplicationScopedBeanInjectSelf getSelf() {
    return self;
  }
}
