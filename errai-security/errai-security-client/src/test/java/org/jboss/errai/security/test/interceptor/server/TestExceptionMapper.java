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

package org.jboss.errai.security.test.interceptor.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.errai.security.server.jaxrs.UnauthenticatedExceptionMapper;
import org.jboss.errai.security.server.jaxrs.UnauthorizedExceptionMapper;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;

/**
 * This class is a work around to test the exception mapping behaviour because
 * of a classloading error that prevents the ExceptionMappers in
 * errai-security-server from being loaded.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Provider
public class TestExceptionMapper implements ExceptionMapper<SecurityException> {
  @Override
  public Response toResponse(final SecurityException exception) {
    if (exception instanceof UnauthenticatedException) {
      return new UnauthenticatedExceptionMapper().toResponse((UnauthenticatedException) exception);
    }
    else if (exception instanceof UnauthorizedException) {
      return new UnauthorizedExceptionMapper().toResponse((UnauthorizedException) exception);
    }
    else {
      throw new IllegalStateException("Unreconized security exception.");
    }
  }

}
