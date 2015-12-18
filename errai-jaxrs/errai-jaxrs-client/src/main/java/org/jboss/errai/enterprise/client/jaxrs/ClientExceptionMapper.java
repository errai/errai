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

package org.jboss.errai.enterprise.client.jaxrs;

import com.google.gwt.http.client.Response;

/**
 * All client-side REST exception mappers must implement this interface.  It
 * provides a way for the developer to have fine-grained control of mapping
 * REST {@link Response} objects to specific types of {@link Exception}s.
 * Typically an implementation would use the response code and payload to 
 * determine the type of error and create a new instance of an exception
 * based on that information.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ClientExceptionMapper {
  
  /**
   * Converts the REST HTTP {@link Response} to an {@link Exception}.
   */
  public Throwable fromResponse(Response response);

}
