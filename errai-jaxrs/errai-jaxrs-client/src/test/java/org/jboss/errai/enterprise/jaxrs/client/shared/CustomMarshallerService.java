package org.jboss.errai.enterprise.jaxrs.client.shared;

import org.jboss.errai.enterprise.jaxrs.client.shared.entity.CustomMarshallerServicePojo;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.Optional;

@Path("/test/custommarshaller")
public interface CustomMarshallerService {

  @POST
  @Path("/post")
  @Produces("text/plain")
  @Consumes("application/json")
  public String post(CustomMarshallerServicePojo pojo);

  @GET
  @Path("/get")
  @Produces("application/json")
  public CustomMarshallerServicePojo get();
}
