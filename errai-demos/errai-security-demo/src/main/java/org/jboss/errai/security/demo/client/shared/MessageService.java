package org.jboss.errai.security.demo.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;

/**
 * @author edewit@redhat.com
 */
@Path("/message")
public interface MessageService {
  @RestrictAccess
  @Path("/hello")
  @GET
  @Produces("application/json")
  String hello();

  @RestrictAccess(roles = "admin")
  @Path("/ping")
  @GET
  @Produces("application/json")
  String ping();
}
