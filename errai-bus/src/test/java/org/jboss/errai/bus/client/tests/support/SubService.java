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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Part of the regression test suite for ERRAI-282. The service method
 * {@link BaseService#baseServiceMethod()} (inherited here but not redeclared)
 * should be visible to and usable by clients.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Remote
public interface SubService extends BaseService {

  /**
   * Returns the number of times this method has been invoked, starting with a
   * return value of 1 for the first invocation.
   */
  int subServiceMethod();

}
