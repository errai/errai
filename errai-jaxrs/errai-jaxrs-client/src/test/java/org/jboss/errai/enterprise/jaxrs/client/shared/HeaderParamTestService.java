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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * This service is used to test support for header parameters (@HeaderParam).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("test/headerparam")
public interface HeaderParamTestService {

  @GET
  @Path("/1")
  public String getWithHeaderParam(@HeaderParam("header") String header);

  @GET 
  @Path("/2")
  public String getWithMultipleHeaderParams(@HeaderParam("header1") String header1, 
      @HeaderParam("header2") Float header2);

  @POST
  public String postWithHeaderParam(String entity, @HeaderParam("header") String header);

  @PUT
  public String putWithHeaderParam(@HeaderParam("header") String header);

  @DELETE
  public String deleteWithHeaderParam(@HeaderParam("header") String header);
  
  @HEAD
  public void headWithHeaderParam(@HeaderParam("header") String header);
}
