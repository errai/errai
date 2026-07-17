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

import java.util.List;
import java.util.Set;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.errai.enterprise.jaxrs.client.shared.entity.EnumMapEntity;

/**
 * This service is used to test support for query parameters (@QueryParam).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/queryparam/")
@Produces(MediaType.APPLICATION_JSON)
public interface QueryParamTestService {

  @GET
  @Path("/1")
  public Long getWithQueryParam(@QueryParam("id") long id);

  @GET
  @Path("/2")
  public String getWithStringQueryParam(@QueryParam("id") String id);
  
  @GET 
  @Path("/3")
  public String getWithMultipleQueryParams(@QueryParam("id1") long id1, @QueryParam("id2") long id2);
  
  @GET
  @Path("/4")
  public List<Long> getWithQueryParamListOfLongs(@QueryParam("id") List<Long> id);
  
  @GET
  @Path("/5")
  public Set<String> getWithQueryParamSetOfStrings(@QueryParam("id") Set<String> id);

  @GET
  @Path("/5t")
  public Set<String> getWithQueryParamSetOfStringsTreeSet(@QueryParam("id") Set<String> id);

  @GET
  @Path("/6")
  public List<String> getWithQueryParamListOfStrings(@QueryParam("id") List<String> id);
  
  @GET
  @Path("/6e")
  public List<EnumMapEntity.SomeEnum> getWithQueryParamListOfEnums(@QueryParam("id") List<EnumMapEntity.SomeEnum> id);
  
  @GET
  @Path("/7")
  public List<String> getWithMultipleQueryParamListOfStrings(@QueryParam("id1") List<String> id1,
      @QueryParam("id2") String id, @QueryParam("id3") List<String> id2);
  
  @GET
  @Path("/8")
  public List<String> getWithMultipleQueryParamsAndListOfStrings(@QueryParam("id1") String id1,
      @QueryParam("id2") List<String> id2, @QueryParam("id3") String id3);

  @POST
  public int postWithQueryParam(String entity, @QueryParam("id") int id);

  @PUT
  public Double putWithQueryParam(@QueryParam("id") Double id);

  @DELETE
  public short deleteWithQueryParam(@QueryParam("id") short id);
  
  @HEAD
  public void headWithQueryParam(@QueryParam("id") long id);
}
