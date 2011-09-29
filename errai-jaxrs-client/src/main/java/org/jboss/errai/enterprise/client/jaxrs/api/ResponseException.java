package org.jboss.errai.enterprise.client.jaxrs.api;

import com.google.gwt.http.client.Response;

/**
 * Indicates an invalid/unexpected HTTP response.
 *  
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ResponseException extends Exception {
  private static final long serialVersionUID = 1L;

  private Response response;
  
  public ResponseException(String message, Response response) {
    super(message);
    this.response = response;
  }
  
  public Response getResponse() {
    return response;
  }
}