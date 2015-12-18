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

package org.jboss.errai.samples.restdemo.client.local;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper;
import org.jboss.errai.samples.restdemo.client.shared.CustomerNotFoundException;

import com.google.gwt.http.client.Response;

/**
 * A simple exception mapper used in the jax-rs demo app.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
public class CustomerServiceExceptionMapper implements ClientExceptionMapper {
  
  /**
   * Constructor.
   */
  public CustomerServiceExceptionMapper() {
  }
  
  /**
   * @see org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper#fromResponse(com.google.gwt.http.client.Response)
   */
  @Override
  public Throwable fromResponse(Response response) {
    return new CustomerNotFoundException(17l);
  }

}
