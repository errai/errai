package org.jboss.errai.ioc.client.api;

public interface TypeProvider<T> {
    public <T> T provide();
}
