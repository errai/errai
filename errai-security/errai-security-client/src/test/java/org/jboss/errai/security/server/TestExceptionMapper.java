package org.jboss.errai.security.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.errai.security.server.jaxrs.UnauthenticatedExceptionMapper;
import org.jboss.errai.security.server.jaxrs.UnauthorizedExceptionMapper;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;

/**
 * This class is a work around to test the exception mapping behaviour because
 * of a classloading error that prevents the ExceptionMappers in
 * errai-security-server from being loaded.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Provider
public class TestExceptionMapper implements ExceptionMapper<SecurityException> {
  @Override
  public Response toResponse(final SecurityException exception) {
    if (exception instanceof UnauthenticatedException) {
      return new UnauthenticatedExceptionMapper().toResponse((UnauthenticatedException) exception);
    }
    else if (exception instanceof UnauthorizedException) {
      return new UnauthorizedExceptionMapper().toResponse((UnauthorizedException) exception);
    }
    else {
      throw new IllegalStateException("Unreconized security exception.");
    }
  }

}
