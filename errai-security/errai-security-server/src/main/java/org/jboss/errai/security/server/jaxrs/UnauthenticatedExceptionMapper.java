package org.jboss.errai.security.server.jaxrs;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.security.shared.exception.UnauthenticatedException;

@Provider
public class UnauthenticatedExceptionMapper extends SecurityJaxrsExceptionMapper<UnauthenticatedException> {
}
