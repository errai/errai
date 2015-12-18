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

package org.jboss.errai.security.client.local.interceptors;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.AbstractJSONClientExceptionMapper;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.security.shared.exception.SecurityException;

import com.google.gwt.http.client.Response;

/**
 * Extracts {@link SecurityException SecurityExceptions} from jax-rs responses.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Provider
public class SecurityExceptionMapper extends AbstractJSONClientExceptionMapper {
  // TODO Add logging.

  @Override
  public Throwable fromResponse(final Response response) {
    if (response.getStatusCode() == 403) {
      SecurityException securityException = null;
      try {
        securityException = fromJSON(response, SecurityException.class);
      }
      catch (RuntimeException e) {
      }

      if (securityException != null) {
        return securityException;
      }
    }

    return new ResponseException(response.getStatusText(), response);
  }

}
