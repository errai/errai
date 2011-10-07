package org.jboss.errai.enterprise.jaxrs.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.enterprise.jaxrs.client.entity.Entity;

@Path("test/customtype")
public interface CustomTypeTestService {

  @GET
  @Produces("application/json")
  public Entity getEntity();
  
  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Entity postEntity(Entity entity);
  
  @PUT
  @Consumes("application/json")
  @Produces("application/json")
  public Entity putEntity(Entity entity);
}
