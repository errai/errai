/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local.storage;

import org.jboss.errai.security.shared.api.identity.User;

/**
 * Stores compile-time configurations for Errai Security.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface SecurityProperties {

  /**
   * @return True iff {@literal ErraiApp.properties} was configured to allow
   *         {@link User Users} to be cached in browser local storage.
   */
  public Boolean isLocalStorageOfUserAllowed();

}
