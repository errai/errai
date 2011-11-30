package org.jboss.errai.enterprise.jaxrs.client.shared;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * This service is used to test all supported HTTP methods.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/method")
public interface PlainMethodTestService {

  @GET 
  public String get();
 
  @POST 
  public String post();
  
  @PUT 
  public String put();

  @DELETE 
  public String delete();
  
  @HEAD
  public String head();
}
