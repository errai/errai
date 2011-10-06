package org.jboss.errai.enterprise.jaxrs.client;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path("test")
public interface TestService {

  @GET
  public String noParamGet();
  
  @POST
  public String noParamPost();
  
  @PUT
  public String noParamPut();
  
  @DELETE
  public String noParamDelete();
 
}
