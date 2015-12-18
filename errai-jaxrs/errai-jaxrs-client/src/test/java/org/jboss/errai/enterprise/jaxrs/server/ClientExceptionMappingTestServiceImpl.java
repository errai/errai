/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.enterprise.jaxrs.client.shared.ClientExceptionMappingTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.UserNotFoundException;

/**
 * Implementation of {@link ClientExceptionMappingTestService}.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClientExceptionMappingTestServiceImpl implements ClientExceptionMappingTestService {

  /**
   * @see org.jboss.errai.enterprise.jaxrs.client.shared.ClientExceptionMappingTestService#getUsername(long)
   */
  @Override
  public String getUsername(long userId) {
    return "csadilek";
  }

  /**
   * @see org.jboss.errai.enterprise.jaxrs.client.shared.ClientExceptionMappingTestService#getUsernameWithError(long)
   */
  @Override
  public String getUsernameWithError(long userId) throws UserNotFoundException {
    throw new UserNotFoundException(userId);
  }

}
