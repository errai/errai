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
@Path("/test/customtype")
public interface CustomTypeTestService {
  public static final List<Entity> ENTITIES = new ArrayList<Entity>() {
    {
      add(new Entity(1, "entity1"));
      add(new Entity(2, "entity2"));
    }
  };
  
  @GET
  @Path("/test/customtype/1")
  @Produces("application/json")
  public Entity getEntity();
  
  @GET
  @Produces("application/json")
  public List<Entity> getEntities();
  
  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Entity postEntity(Entity entity);
  
  @PUT
  @Consumes("application/json")
  @Produces("application/json")
  public Entity putEntity(Entity entity);
  
  @DELETE
  @Path("/{id}")
  @Produces("application/json")
  public Entity deleteEntity(@PathParam("id") long id);
}
