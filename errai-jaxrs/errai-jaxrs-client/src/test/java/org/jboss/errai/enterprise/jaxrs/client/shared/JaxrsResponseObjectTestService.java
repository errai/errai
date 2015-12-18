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

package org.jboss.errai.enterprise.jaxrs.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This service is used to test JAX-RS methods returning a {@link javax.ws.rs.core.Response} object
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/jaxrs-response-object")
public interface JaxrsResponseObjectTestService {

  @GET
  @Produces("application/json")
  public Response get();
 
  @GET
  @Path("/error")
  public Response getReturningError();
  
  @POST
  @Produces("text/plain")
  public Response post(String entity);
}
