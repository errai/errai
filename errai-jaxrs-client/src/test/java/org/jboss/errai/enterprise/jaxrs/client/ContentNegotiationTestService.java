package org.jboss.errai.enterprise.jaxrs.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("test/contentnegotiation")
public interface ContentNegotiationTestService {

  @GET
  @Produces("text/plain")
  public String getText();

  @GET
  @Produces("application/xml")
  public String getXml();
  
  @POST
  @Consumes("text/*")
  public String postText(String text);

  @POST
  @Consumes("application/xml")
  public String postXml(String xml);

  @PUT
  @Consumes("text/plain")
  public String putText(String text);
  
  @PUT
  @Consumes("application/*")
  public String putXml(String xml);

  @DELETE
  @Consumes("text/plain")
  public String deleteText(String text);
  
  @DELETE
  @Consumes("application/xml")
  public String deleteXml(String xml);
}
