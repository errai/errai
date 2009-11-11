package org.jboss.errai.common.client.types;

import com.google.gwt.json.client.JSONObject;

public interface Demarshaller<T> {
    public T demarshall(JSONObject o); 
}
