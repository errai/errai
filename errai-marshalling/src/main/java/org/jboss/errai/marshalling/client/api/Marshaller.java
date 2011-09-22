package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Marshaller<T> {
    Class<?> getTypeHandled();
    
    String getEncodingType();
    
    T demarshall(Object o, MarshallingContext ctx);
    
    Object marshall(T o, MarshallingContext ctx);
}
