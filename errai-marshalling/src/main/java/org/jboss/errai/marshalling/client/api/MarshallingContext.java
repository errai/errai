package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallingContext {
//  public void pushElementMarshaller(Marshaller<?, ?> marshaller);
//  public Marshaller<?, ?> peekElementMarshaller();
//  public Marshaller<?, ?> popElementMarshaller();

  public Marshaller<Object, Object> getMarshallerForType(String fqcn);
  
  public String determineTypeFor(String formatType, Object o);
}
