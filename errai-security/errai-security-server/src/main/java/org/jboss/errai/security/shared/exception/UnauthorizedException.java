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

package org.jboss.errai.security.shared.exception;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown when the user attempts to access a resource which requires greater
 * privileges than the user possesses.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class UnauthorizedException extends SecurityException {

  private static final long serialVersionUID = 1L;

  public UnauthorizedException() {
    super();
  }

  public UnauthorizedException(final String message) {
    super(message);
  }

  public UnauthorizedException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
