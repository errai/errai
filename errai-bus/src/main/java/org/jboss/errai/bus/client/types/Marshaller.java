package org.jboss.errai.bus.client.types;

public interface Marshaller<T> {
    public String marshall(T object);
}
