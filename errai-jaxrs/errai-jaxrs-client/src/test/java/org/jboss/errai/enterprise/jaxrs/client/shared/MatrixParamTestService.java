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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * This service is used to test support for matrix parameters (@MatrixParam).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/matrixparam")
public interface MatrixParamTestService {

  @GET 
  @Path("/{id}")
  public String getWithSingleMatrixParam(@PathParam("id") String path, @MatrixParam("first") String first);

  @GET 
  public String getWithMatrixParams(@MatrixParam("first") long first, @MatrixParam("last") long last);

  @POST
  public String postWithMatrixParams(String entity, @MatrixParam("first") String first, @MatrixParam("last") String last);
  
  @PUT
  public String putWithMatrixParams(@MatrixParam("first") String first, @MatrixParam("middle") String middle, 
      @MatrixParam("last") String last);

  @DELETE
  public String deleteWithMatrixParams(@MatrixParam("first") String first, @MatrixParam("last") String last);
  
  @HEAD
  public void headWithMatrixParams(@MatrixParam("first") String first, @MatrixParam("middle") String middle, 
      @MatrixParam("last") String last);
}
