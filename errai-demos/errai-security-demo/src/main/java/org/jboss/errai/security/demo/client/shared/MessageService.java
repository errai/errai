package org.jboss.errai.security.demo.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * @author edewit@redhat.com
 */
@Path("/message")
public interface MessageService {
  @RestrictedAccess
  @Path("/hello")
  @GET
  @Produces("application/json")
  String hello();

  @RestrictedAccess(roles = "admin")
  @Path("/ping")
  @GET
  @Produces("application/json")
  String ping();
}
