package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Finishable<T> {
    public T finish();
}
