package org.jboss.errai.enterprise.jaxrs.client.shared;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/test/queryparam")
public interface QueryParamTestService {

  @GET
  @Path("/1")
  public long getWithQueryParam(@QueryParam("id") long id);

  @GET 
  @Path("/2")
  public String getWithMultipleQueryParams(@QueryParam("id1") long id1, @QueryParam("id2") long id2);

  @POST
  public long postWithQueryParam(@QueryParam("id") long id);

  @PUT
  public long putWithQueryParam(@QueryParam("id") long id);

  @DELETE
  public long deleteWithQueryParam(@QueryParam("id") long id);
  
  @HEAD
  public void headWithQueryParam(@QueryParam("id") long id);
}
