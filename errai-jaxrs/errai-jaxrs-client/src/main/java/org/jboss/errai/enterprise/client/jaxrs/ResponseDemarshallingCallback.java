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

package org.jboss.errai.enterprise.client.jaxrs;

/**
 * A callback used by the generated JAX-RS proxies to demarshall the response of an HTTP request.
 * 
 * The reason proxies carry out marshalling and demarshalling directly is that the type and content-type information
 * can be inferred statically (at compile time). There is no need to defer the discussion on how to
 * demarshall a response to run time.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * 
 * @param <T>
 *          The type the response is demarshalled to.
 */
public interface ResponseDemarshallingCallback<T> {

  /**
   * Demarshalls the provided response into an object of type <T>.
   * 
   * @param String
   *          representation of the response, must not be null.
   * @return the demarshalled object.
   */
  public T demarshallResponse(String response);
}
