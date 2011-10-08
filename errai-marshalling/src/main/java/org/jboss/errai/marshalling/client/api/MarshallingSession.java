package org.jboss.errai.marshalling.client.api;

import com.google.gwt.json.client.JSONValue;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallingSession {
  public Marshaller<Object, Object> getMarshallerForType(String fqcn);
  
  public Marshaller<Object, Object> getArrayMarshallerForType(String fqcn);
  
  public String marshall(Object o);
  
  public <T> T demarshall(Class<T> clazz, Object o);
  
  public String determineTypeFor(String formatType, Object o);
  
  public boolean hasObjectHash(String hashCode);
  
  public <T> T getObject(Class<T> type, String hashCode);
  
  public void recordObjectHash(String hashCode, Object instance);

  //todo: list of available context.
}
