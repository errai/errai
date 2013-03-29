package org.jboss.errai.aerogear.api.pipeline;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * This class wraps a result and provides methods for retrieving the next and previous result sets.
 * 
 * @param <T> the data type of the list
 */
public interface PagedList<T> extends List<T> {

    /**
     * Retrieve the next result set.  This method MUST NOT pass data to the callback which can not be used.
     * 
     * @param callback 
     */
    public void next(AsyncCallback<List<T>> callback);

    /**
     * Retrieve the previous result set.  This method MUST NOT pass data to the callback which can not be used.
     * 
     * @param callback 
     */
    public void previous(AsyncCallback<List<T>> callback);
}
