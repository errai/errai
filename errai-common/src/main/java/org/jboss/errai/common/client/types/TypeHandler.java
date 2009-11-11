package org.jboss.errai.common.client.types;

public interface TypeHandler<V, T> {
    public T getConverted(V in);
}
