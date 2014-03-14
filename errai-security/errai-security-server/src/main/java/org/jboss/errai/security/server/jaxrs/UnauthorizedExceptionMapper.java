package org.jboss.errai.security.server.jaxrs;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.security.shared.exception.UnauthorizedException;

@Provider
public class UnauthorizedExceptionMapper extends SecurityJaxrsExceptionMapper<UnauthorizedException> {
}
