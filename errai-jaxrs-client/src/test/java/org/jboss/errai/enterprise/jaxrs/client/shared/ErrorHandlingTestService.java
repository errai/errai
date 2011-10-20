package org.jboss.errai.enterprise.jaxrs.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/test/errorhandling")
public interface ErrorHandlingTestService {

  @GET
  @Produces("application/json")
  public long error();
 
}
