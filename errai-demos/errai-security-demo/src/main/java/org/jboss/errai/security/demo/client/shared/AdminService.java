package org.jboss.errai.security.demo.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * This is a secured JAX-RS endpoint. The {@link AdminRoleProvider} will return the "admin"
 * {@link Role} at runtime, so this role is required to access the service. Because the
 * {@link RestrictedAccess} annotation is on the type, any methods added to this type would also be
 * secured. It is also possible to annotate individual methods for finer-grained control, or to
 * annotate both the type and methods (in which case the roles will be combined an all roles must be
 * present for access to be granted).
 */
@Path("/admin")
@RestrictedAccess(providers = { AdminRoleProvider.class })
public interface AdminService {

  @Path("/ping")
  @GET
  @Produces("application/json")
  String ping();
}
