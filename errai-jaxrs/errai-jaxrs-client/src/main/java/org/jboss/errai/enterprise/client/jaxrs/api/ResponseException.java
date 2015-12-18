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

package org.jboss.errai.enterprise.client.jaxrs.api;

import com.google.gwt.http.client.Response;

/**
 * Indicates an invalid/unexpected HTTP response.
 *  
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ResponseException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private Response response;
  
  public ResponseException(String message, Response response) {
    super(message);
    this.response = response;
  }
  
  public Response getResponse() {
    return response;
  }
}
