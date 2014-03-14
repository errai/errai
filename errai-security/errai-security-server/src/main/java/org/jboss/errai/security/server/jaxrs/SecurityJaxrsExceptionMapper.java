package org.jboss.errai.security.server.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.jboss.errai.security.shared.exception.SecurityException;

abstract class SecurityJaxrsExceptionMapper<E extends SecurityException> implements ExceptionMapper<E> {

  @Override
  public Response toResponse(final E exception) {
    return Response.status(Status.FORBIDDEN).entity(exception).build();
  }

}
