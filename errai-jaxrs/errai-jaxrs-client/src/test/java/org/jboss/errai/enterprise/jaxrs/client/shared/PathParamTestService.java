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

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.PathSegment;

/**
 * This service is used to test support for path parameters (@PathParam).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/pathparam")
@Produces("text/plain")
public interface PathParamTestService {
  
  @GET 
  @Path("/t1/{id}")
  public long getWithPathParam(@PathParam("id") long id);
  
  @GET 
  @Path("/t1/{id:[0-9][0-9]*}")
  public long getWithPathParamRegex(@PathParam("id") long id);

  @GET 
  @Path("/t2/{id}")
  public String getWithStringPathParam(@PathParam("id") String id);
  
  @GET 
  @Path("/t3/{id}")
  public String getWithPathSegmentPathParam(@PathParam("id") PathSegment id);
  
  @GET 
  @Path("/t4/{date}")
  public String getWithDatePathParam(@PathParam("date") Date date);
  
  @GET 
  @Path("/{id1}/{id2}")
  public String getWithMultiplePathParams(@PathParam("id1") int id1, @PathParam("id2") int id2);

  @GET 
  @Path("/{id1}/{id2}/{id1}")
  public String getWithReusedPathParam(@PathParam("id1") double id1, @PathParam("id2") double id2);
  
  @POST
  @Path("/{id}")
  @Consumes("text/plain")
  public Float postWithPathParam(String entity, @PathParam("id") Float id);

  @PUT
  @Path("/{id}")
  @Consumes("text/plain")
  public long putWithPathParam(Long entity, @PathParam("id") long id);

  @DELETE
  @Path("/{id}")
  public long deleteWithPathParam(@PathParam("id") long id);
  
  @HEAD
  @Path("/{id}")
  public void headWithPathParam(@PathParam("id") long id);
}
