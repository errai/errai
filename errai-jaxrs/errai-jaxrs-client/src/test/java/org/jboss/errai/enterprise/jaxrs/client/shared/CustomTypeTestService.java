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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.errai.enterprise.jaxrs.client.shared.entity.Entity;

/**
 * This service is used to test sending entities of custom types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("test/customtype")
public interface CustomTypeTestService {
  @SuppressWarnings("serial")
  public static final List<Entity> ENTITIES = new ArrayList<Entity>() {
    {
      add(new Entity(1, "entity1"));
      add(new Entity(2, "entity2"));
    }
  };
  
  @GET
  @Path("/entity")
  @Produces("application/json")
  public Entity getEntity();
  
  @GET
  @Path("/sub")
  @Produces("application/json")
  public Entity getSubEntity();
  
  @GET
  @Produces("application/json")
  public List<Entity> getEntities();
  
  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Entity postEntity(Entity entity);
  
  @POST
  @Path("/null")
  @Consumes("application/json")
  @Produces("application/json")
  public Entity postEntityReturningNull(Entity entity);
  
  @POST
  @Consumes("application/entity+json")
  @Produces("application/entity+json")
  public Entity postEntityCustomJsonMediaType(Entity entity);
  
  @PUT
  @Consumes("application/json")
  @Produces("application/json")
  public Entity putEntity(Entity entity);
  
  @PUT
  @Path("/void")
  @Consumes("application/json")
  @Produces("application/json")
  public void putEntityReturningVoid(Entity entity);
  
  @DELETE
  @Path("/{id}")
  @Produces("application/json")
  public Entity deleteEntity(@PathParam("id") long id);
}
