package org.jboss.errai.enterprise.jaxrs.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * This service is used to test error handling features. This interface is only used to generate proxies, it has no 
 * implementation. Therefore, any attempt in calling a method will result in an HTTP error 404. 
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/errorhandling")
public interface ErrorHandlingTestService {

  @GET
  @Produces("application/json")
  public long error();
 
}
