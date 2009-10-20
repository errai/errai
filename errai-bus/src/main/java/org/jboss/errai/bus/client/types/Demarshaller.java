package org.jboss.errai.bus.client.types;

import com.google.gwt.json.client.JSONObject;

public interface Demarshaller<T> {
    public T demarshall(JSONObject o); 
}
