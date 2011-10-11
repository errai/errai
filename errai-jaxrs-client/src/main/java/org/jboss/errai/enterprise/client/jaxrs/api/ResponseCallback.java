package org.jboss.errai.enterprise.client.jaxrs.api;

import org.jboss.errai.bus.client.api.RemoteCallback;

import com.google.gwt.http.client.Response;

/**
 * A {@link RemoteCallback} that can be used to retrieve the underlying HTTP response.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ResponseCallback extends RemoteCallback<Response> {

  
}
