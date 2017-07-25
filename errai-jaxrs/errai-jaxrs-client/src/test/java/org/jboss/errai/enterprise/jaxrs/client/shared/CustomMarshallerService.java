package org.jboss.errai.enterprise.jaxrs.client.shared;

import org.jboss.errai.enterprise.jaxrs.client.shared.entity.CustomMarshallerServicePojo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
