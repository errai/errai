package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallingSession {
  public Marshaller<Object, Object> getMarshallerForType(String fqcn);
  
  public String determineTypeFor(String formatType, Object o);

  //todo: list of available context.
}
