package org.jboss.errai.security.client.local.interceptors;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.AbstractJSONClientExceptionMapper;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.security.shared.exception.SecurityException;

import com.google.gwt.http.client.Response;

/**
 * Extracts {@link SecurityException SecurityExceptions} from jax-rs responses.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Provider
public class SecurityExceptionMapper extends AbstractJSONClientExceptionMapper {
  // TODO Add logging.

  @Override
  public Throwable fromResponse(final Response response) {
    if (response.getStatusCode() == 403) {
      SecurityException securityException = null;
      try {
        securityException = fromJSON(response, SecurityException.class);
      }
      catch (RuntimeException e) {
      }

      if (securityException != null) {
        return securityException;
      }
    }

    return new ResponseException(response.getStatusText(), response);
  }

}
