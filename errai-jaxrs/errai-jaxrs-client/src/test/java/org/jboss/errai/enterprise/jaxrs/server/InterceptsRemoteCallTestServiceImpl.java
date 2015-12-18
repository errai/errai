/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.InterceptsRemoteCallTestService;

/**
 * Implementation of {@link InterceptsRemoteCallTestService} returning test data.
 */
public class InterceptsRemoteCallTestServiceImpl implements InterceptsRemoteCallTestService {
  
  /**
   * @see org.jboss.errai.enterprise.jaxrs.client.shared.InterceptsRemoteCallTestService#interceptedGet1()
   */
  @Override
  public String interceptedGet1() {
    // should never be called
    return "not intercepted";
  }
  
  /**
   * @see org.jboss.errai.enterprise.jaxrs.client.shared.InterceptsRemoteCallTestService#interceptedGet2()
   */
  @Override
  public String interceptedGet2() {
    // should never be called
    return "not intercepted";
  }
  
}
