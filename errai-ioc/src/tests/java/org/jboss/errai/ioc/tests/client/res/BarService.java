package org.jboss.errai.ioc.tests.client.res;

/**
 * @author Mike Brock .
 */
public interface BarService<T> {
    public T get();
}
