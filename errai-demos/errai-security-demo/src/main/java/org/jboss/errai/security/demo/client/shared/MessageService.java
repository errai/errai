package org.jboss.errai.security.demo.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;

/**
 * @author edewit@redhat.com
 */
@Path("/message")
public interface MessageService {
  @RequireAuthentication
  @Path("/hello")
  @GET
  @Produces("application/json")
  String hello();

  @RequireRoles("admin")
  @Path("/ping")
  @GET
  @Produces("application/json")
  String ping();
}
