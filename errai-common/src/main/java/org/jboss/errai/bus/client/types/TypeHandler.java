package org.jboss.errai.bus.client.types;

public interface TypeHandler<V, T> {
    public T getConverted(V in);
}
