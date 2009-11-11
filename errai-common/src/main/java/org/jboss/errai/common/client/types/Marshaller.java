package org.jboss.errai.common.client.types;

public interface Marshaller<T> {
    public String marshall(T object);
}
