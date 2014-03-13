package org.jboss.errai.security.demo.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;

/**
 * @author edewit@redhat.com
 */
@Path("message")
public interface MessageService {
  @RequireAuthentication
  @GET
  @Produces("application/json")
  String hello();

  @RequireRoles("admin")
  @GET
  @Produces("application/json")
  String ping();
}
