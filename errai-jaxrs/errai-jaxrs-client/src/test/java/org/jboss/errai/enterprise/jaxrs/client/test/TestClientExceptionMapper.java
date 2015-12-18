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

package org.jboss.errai.enterprise.jaxrs.client.test;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper;
import org.jboss.errai.enterprise.jaxrs.client.shared.ClientExceptionMappingTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.UserNotFoundException;
import org.jboss.errai.enterprise.shared.api.annotations.MapsFrom;

import com.google.gwt.http.client.Response;

/**
 * Client-side exception mapper for testing purposes.
 * 
 * @author eric.wittmann@redhat.com
 */
@Provider
@MapsFrom({ ClientExceptionMappingTestService.class })
public class TestClientExceptionMapper implements ClientExceptionMapper {

  /**
   * Constructor.
   */
  public TestClientExceptionMapper() {}

  /**
   * @see org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper#fromResponse(com.google.gwt.http.client.Response)
   */
  @Override
  public Throwable fromResponse(Response response) {
    return new UserNotFoundException(-1);
  }

}
